package com.church.churchapp.config;

import com.church.churchapp.entity.AuditLog;
import com.church.churchapp.repository.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AuditLogFilter extends OncePerRequestFilter {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ChurchGroupRepository churchGroupRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();
        int status = response.getStatus();

        boolean isWriteMethod = method.equals("POST") || method.equals("PUT") || method.equals("DELETE");
        boolean isMultipart = request.getContentType() != null && request.getContentType().contains("multipart");

        // Wrap the request to enable content caching for POST/PUT (non-multipart)
        HttpServletRequest requestToUse = request;
        if (isWriteMethod && !isMultipart && (method.equals("POST") || method.equals("PUT"))) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        // Pre-resolve targeted entity details before the actual database deletion or update happens
        String targetEntityDetails = null;
        if (method.equals("DELETE") || method.equals("PUT")) {
            targetEntityDetails = fetchTargetEntityDetails(method, path, requestToUse);
        }

        filterChain.doFilter(requestToUse, response);

        status = response.getStatus();

        // Log modifications (POST, PUT, DELETE) that were successful (status 2xx or 3xx)
        if (isWriteMethod && status >= 200 && status < 400) {
            
            if (path.contains("/api/login")) {
                return;
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) 
                    ? auth.getName() 
                    : "Anonymous";

            String action = getActionName(method, path);
            String details = "";

            if (method.equals("DELETE")) {
                details = targetEntityDetails != null ? targetEntityDetails : "Deleted item at: " + path;
            } else {
                String payload = "";
                if (isMultipart) {
                    String memberData = requestToUse.getParameter("memberData");
                    if (memberData != null && !memberData.trim().isEmpty()) {
                        payload = obfuscatePasswords(memberData);
                    } else {
                        StringBuilder params = new StringBuilder("Multipart request params:\n");
                        requestToUse.getParameterMap().forEach((k, v) -> {
                            if (!k.equals("photo") && !k.toLowerCase().contains("password")) {
                                params.append(k).append(": ").append(String.join(", ", v)).append("\n");
                            }
                        });
                        payload = params.toString().trim();
                        if (payload.equals("Multipart request params:")) {
                            payload = "Multipart update submitted (binary data)";
                        }
                    }
                } else if (requestToUse instanceof ContentCachingRequestWrapper) {
                    ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) requestToUse;
                    byte[] buf = wrapper.getContentAsByteArray();
                    if (buf.length > 0) {
                        try {
                            String charEncoding = wrapper.getCharacterEncoding() != null ? wrapper.getCharacterEncoding() : "UTF-8";
                            payload = new String(buf, 0, buf.length, charEncoding);
                            payload = obfuscatePasswords(payload);
                        } catch (Exception e) {
                            payload = "Failed to parse body: " + e.getMessage();
                        }
                    }
                }

                if (method.equals("PUT") && targetEntityDetails != null) {
                    Object targetObject = requestToUse.getAttribute("targetEntityObject");
                    String diff = computeDiff(targetObject, payload);
                    details = targetEntityDetails + "\n\n" + diff;
                } else {
                    details = payload;
                }
            }

            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setAction(action);
            log.setEndpoint(path);
            log.setMethod(method);
            log.setResponseStatus(status);
            log.setTimestamp(LocalDateTime.now());
            log.setDetails(details);
            
            auditLogRepository.save(log);
        }
    }

    private String obfuscatePasswords(String payload) {
        if (payload == null) return null;
        // Obfuscate plain string password or json password field
        return payload.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"******\"")
                      .replaceAll("password=[^&]*", "password=******");
    }

    private String computeDiff(Object originalEntity, String jsonPayload) {
        if (originalEntity == null || jsonPayload == null || jsonPayload.trim().isEmpty()) {
            return "No payload or original data available to compute changes.";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonPayload);
            StringBuilder diff = new StringBuilder();
            
            Class<?> clazz = originalEntity.getClass();
            
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode newValueNode = field.getValue();
                
                // Ignore audit/system fields or binary/huge fields
                if (fieldName.equals("password") || fieldName.equals("profilePhoto") || fieldName.equals("photo")) {
                    continue;
                }
                
                // Skip the entity's own primary key (e.g. memberId for Member, userId for User)
                String className = clazz.getSimpleName().toLowerCase();
                if (fieldName.equals("id") || fieldName.toLowerCase().equals(className + "id")) {
                    continue;
                }
                
                // Construct getter method name
                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method getter = null;
                try {
                    getter = clazz.getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    // Try "is" prefix for booleans
                    String isGetterName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                    try {
                        getter = clazz.getMethod(isGetterName);
                    } catch (NoSuchMethodException ex) {
                        continue;
                    }
                }
                
                Object oldValue = getter.invoke(originalEntity);
                String oldValueStr = getSimpleStringValue(oldValue);
                String newValueStr = getJsonNodeStringValue(newValueNode);
                
                // Compare values
                if (!oldValueStr.equals(newValueStr)) {
                    // Normalize empty strings vs null to avoid noise
                    if ((oldValueStr.equals("null") || oldValueStr.isEmpty()) && 
                        (newValueStr.equals("null") || newValueStr.isEmpty())) {
                        continue;
                    }
                    diff.append("- ").append(fieldName).append(": '")
                        .append(oldValueStr).append("' -> '").append(newValueStr).append("'\n");
                }
            }
            
            if (diff.length() > 0) {
                return "Updated fields:\n" + diff.toString().trim();
            } else {
                return "No fields changed (values are identical).";
            }
        } catch (Exception e) {
            return "Unable to compute changes: " + e.getMessage();
        }
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || 
               type.equals(String.class) || 
               type.equals(Integer.class) || 
               type.equals(Long.class) || 
               type.equals(Double.class) || 
               type.equals(Float.class) || 
               type.equals(Boolean.class) || 
               type.isEnum();
    }

    private String getSimpleStringValue(Object obj) {
        if (obj == null) return "null";
        
        if (obj instanceof java.util.Collection) {
            java.util.Collection<?> col = (java.util.Collection<?>) obj;
            java.util.List<String> items = new java.util.ArrayList<>();
            for (Object item : col) {
                if (item == null) continue;
                if (isSimpleType(item.getClass())) {
                    items.add(item.toString().trim());
                } else {
                    items.add(extractNameFromEntity(item));
                }
            }
            java.util.Collections.sort(items);
            return items.toString();
        }
        
        if (!isSimpleType(obj.getClass())) {
            return extractNameFromEntity(obj);
        }
        
        return obj.toString().trim();
    }

    private String extractNameFromEntity(Object entity) {
        if (entity == null) return "null";
        Class<?> clazz = entity.getClass();
        String[] possibleGetters = {"getName", "getMinistryName", "getTitle", "getFileName", "getFamilyName"};
        for (String getterName : possibleGetters) {
            try {
                Method m = clazz.getMethod(getterName);
                Object val = m.invoke(entity);
                if (val != null) return val.toString().trim();
            } catch (Exception ignored) {}
        }
        try {
            Method m = clazz.getMethod("getId");
            Object val = m.invoke(entity);
            if (val != null) return val.toString().trim();
        } catch (Exception ignored) {}
        
        return entity.toString().trim();
    }

    private String getJsonNodeStringValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return "null";
        }
        if (node.isArray()) {
            java.util.List<String> items = new java.util.ArrayList<>();
            for (JsonNode item : node) {
                items.add(item.asText().trim());
            }
            java.util.Collections.sort(items);
            return items.toString();
        }
        if (node.isObject()) {
            JsonNode nameNode = node.get("name");
            if (nameNode == null) nameNode = node.get("ministryName");
            if (nameNode == null) nameNode = node.get("familyName");
            if (nameNode == null) nameNode = node.get("title");
            if (nameNode != null) return nameNode.asText().trim();
            
            JsonNode idNode = node.get("id");
            if (idNode != null) return idNode.asText().trim();
        }
        return node.asText().trim();
    }

    private String fetchTargetEntityDetails(String method, String path, HttpServletRequest request) {
        try {
            if (path == null) return null;
            
            Pattern pattern = Pattern.compile("/(\\d+)$");
            Matcher matcher = pattern.matcher(path);
            if (!matcher.find()) {
                return null;
            }
            
            Long id = Long.parseLong(matcher.group(1));
            String actionVerb = method.equals("DELETE") ? "deleted" : "targeted";
            
            if (path.contains("/api/users")) {
                userRepository.findById(id).ifPresent(u -> request.setAttribute("targetEntityObject", u));
                return userRepository.findById(id)
                    .map(u -> "User " + actionVerb + " - Username: " + u.getUsername() + ", Roles: " + u.getRoles())
                    .orElse("User ID: " + id);
            }
            
            if (path.contains("/api/members")) {
                memberRepository.findById(id).ifPresent(m -> request.setAttribute("targetEntityObject", m));
                return memberRepository.findById(id)
                    .map(m -> "Member " + actionVerb + " - Name: " + m.getFirstName() + " " + m.getLastName() + ", Email: " + m.getEmail() + ", Phone: " + m.getContactNumber())
                    .orElse("Member ID: " + id);
            }
            
            if (path.contains("/api/assets")) {
                assetRepository.findById(id).ifPresent(a -> request.setAttribute("targetEntityObject", a));
                return assetRepository.findById(id)
                    .map(a -> "Asset " + actionVerb + " - Name: " + a.getName() + ", Category: " + a.getCategory() + ", Status: " + a.getStatus())
                    .orElse("Asset ID: " + id);
            }
            
            if (path.contains("/api/groups")) {
                churchGroupRepository.findById(id).ifPresent(g -> request.setAttribute("targetEntityObject", g));
                return churchGroupRepository.findById(id)
                    .map(g -> "Group " + actionVerb + " - Name: " + g.getName() + ", Category: " + g.getCategory())
                    .orElse("Group ID: " + id);
            }
            
            if (path.contains("/api/events")) {
                eventRepository.findById(id).ifPresent(e -> request.setAttribute("targetEntityObject", e));
                return eventRepository.findById(id)
                    .map(e -> "Event " + actionVerb + " - Title: " + e.getTitle() + ", Start Date: " + e.getStartDate())
                    .orElse("Event ID: " + id);
            }
            
            if (path.contains("/api/documents")) {
                documentRepository.findById(id).ifPresent(d -> request.setAttribute("targetEntityObject", d));
                return documentRepository.findById(id)
                    .map(d -> "Document " + actionVerb + " - File: " + d.getFileName() + ", Type: " + d.getFileType())
                    .orElse("Document ID: " + id);
            }
            
            if (path.contains("/api/announcements")) {
                announcementRepository.findById(id).ifPresent(a -> request.setAttribute("targetEntityObject", a));
                return announcementRepository.findById(id)
                    .map(a -> "Announcement " + actionVerb + " - Title: " + a.getTitle())
                    .orElse("Announcement ID: " + id);
            }
            
            if (path.contains("/api/donations") || path.contains("/api/accounting")) {
                transactionRepository.findById(id).ifPresent(t -> request.setAttribute("targetEntityObject", t));
                return transactionRepository.findById(id)
                    .map(t -> "Transaction " + actionVerb + " - Amount: " + t.getAmount() + ", Type: " + t.getType() + ", Date: " + t.getDate())
                    .orElse("Transaction ID: " + id);
            }
            
            if (path.contains("/api/families")) {
                familyRepository.findById(id).ifPresent(f -> request.setAttribute("targetEntityObject", f));
                return familyRepository.findById(id)
                    .map(f -> "Family " + actionVerb + " - Name: " + f.getFamilyName())
                    .orElse("Family ID: " + id);
            }
        } catch (Exception e) {
            return "Error resolving targeted entity: " + e.getMessage();
        }
        return null;
    }

    private String getActionName(String method, String path) {
        if (path.contains("/api/users/create")) return "Created User Account";
        if (path.contains("/api/users") && method.equals("PUT")) return "Updated User Profile / Roles";
        if (path.contains("/api/users") && method.equals("DELETE")) return "Deleted User Account";
        if (path.contains("/api/settings")) return "Modified System Settings";
        if (path.contains("/api/members/add") || path.contains("/api/members/register")) return "Added New Member";
        if (path.contains("/api/members") && method.equals("DELETE")) return "Deleted Member Profile";
        if (path.contains("/api/members") && method.equals("PUT")) return "Updated Member Profile";
        if (path.contains("/api/donations")) return "Recorded Financial Donation";
        if (path.contains("/api/assets")) return "Modified Asset Inventory";
        if (path.contains("/api/groups")) return "Updated Church Groups";
        if (path.contains("/api/events")) return "Created/Modified Event Calendar";
        if (path.contains("/api/documents")) return "Uploaded/Modified Documents";
        if (path.contains("/api/announcements")) return "Published Announcement";
        return method + " " + path;
    }
}
