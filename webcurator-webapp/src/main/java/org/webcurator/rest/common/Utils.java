package org.webcurator.rest.common;

import org.webcurator.rest.dto.ProfileDTO;

import java.lang.reflect.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Utils {

    /**
     * Generate a map with an error message, to be used everywhere in the API
     */
    public static HashMap<String, Object> errorMessage(Object msg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Error", msg);
        return map;
    }

    /**
     * Map the supplied HashMap to a similarly structured DTO
     *
     * This is slightly more flexible than relying on Spring/Jackson to do the mapping for us.
     */
    public static void mapToDTO(HashMap<String, Object> map, Object dto) throws BadRequestError {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            String getMethodName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
            Object child = null;
            Method getMethod = null;
            try {
                getMethod = dto.getClass().getMethod(getMethodName);
            } catch (NoSuchMethodException e) {
                throw new BadRequestError(String.format("Uknown key %s", key));
            }
            try {
                child = getMethod.invoke(dto);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new BadRequestError(e.getMessage());
            }
            Class childType = getMethod.getReturnType(); // child.getClass() may return a narrower type
            if (value instanceof HashMap) { // recurse
                mapToDTO((HashMap<String, Object>) value, child);
            } else if (value instanceof List) {
                if (key.equals("overrides")) { // The list of overrides is fixed, so we can locate and update its elements
                    for (Object element : (List) value) {
                        if (!(element instanceof HashMap)) {
                            throw new BadRequestError("Bad item in list of profile overrides");
                        }
                        ProfileDTO.Override overrideToBeUpdated = null;
                        for (ProfileDTO.Override override : (List<ProfileDTO.Override>) child) {
                            if (override.getId().equals(((HashMap) element).get("id"))) {
                                overrideToBeUpdated = override;
                                break;
                            }
                        }
                        if (overrideToBeUpdated == null) {
                            throw new BadRequestError(String.format("Uknown override with id %s", ((HashMap) element).get("id")));
                        }
                        mapToDTO((HashMap<String, Object>) element, overrideToBeUpdated);
                    }
                } else { // For all other types of lists: clear the list and fill it with the new supplied data
                    ((List) child).clear();
                    Type genericChildType = getMethod.getGenericReturnType();
                    Type elementType;
                    if (genericChildType instanceof ParameterizedType) {
                        elementType = ((ParameterizedType) genericChildType).getActualTypeArguments()[0];
                    } else { // A non-parameterized List is guaranteed to be a list with Strings (URL Strings in overrides)
                        elementType = (Type) String.class;
                    }
                    for (Object element : (List) value) {
                        if (element instanceof HashMap) {
                            try {
                                Constructor constructor = ((Class<?>) elementType).getDeclaredConstructor();
                                constructor.setAccessible(true);
                                Object newArrayItem = constructor.newInstance();
                                ((List) child).add(newArrayItem);
                                mapToDTO((HashMap<String, Object>) element, newArrayItem);
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                                     InstantiationException e) {
                                throw new BadRequestError(String.format("Cannot map list from key %s", key));
                            }
                        } else {
                            // Since the list doesn't contain HashMaps, it contains Numbers or Strings
                            if (elementType.equals(Long.class) && element instanceof Integer) {
                                element = ((Number) element).longValue();
                            } else if (elementType.equals(Integer.class) && element instanceof Long) {
                                element = ((Number) element).intValue();
                            }
                            if (!elementType.equals(element.getClass())) {
                                throw new BadRequestError(String.format("Bad type in key %s", key));
                            }
                            ((List) child).add(element);
                        }
                    }
                }
            } else {
                if (value != null && (key.contains("Date") || key.contains("date"))) {
                    List<String> patterns = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd HH:mm:ss.SSSX",
                            "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSS",
                            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss");
                    for (String pattern : patterns) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                            sdf.setLenient(false);
                            value = sdf.parse((String) value);
                            break;
                        } catch (ParseException e) {
                        }
                    }
                    if (!(value instanceof Date)) {
                        throw new BadRequestError(String.format("Badly formatted date in key %s", key));
                    }
                } else if (childType == Long.class && value instanceof Integer) {
                    value = ((Number) value).longValue();
                } else if (childType == Integer.class && value instanceof Long) {
                    value = ((Number) value).intValue();
                } else if (childType == Long.class && value instanceof String) {
                    value = Long.valueOf((String)value);
                } else if (childType == Integer.class && value instanceof String) {
                    value = Integer.valueOf((String)value);
                } else if (childType == Boolean.class && value instanceof String) {
                    value = Boolean.valueOf((String) value);
                }
                String setMethodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
                try {
                    Method setMethod = dto.getClass().getMethod(setMethodName, childType);
                    try {
                        setMethod.invoke(dto, value);
                    } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                        throw new BadRequestError(e.getMessage());
                    }
                } catch (NoSuchMethodException e) {
                    throw new BadRequestError(String.format("Unknown key or bad type in key %s", key));
                }
            }
        }
    }
}
