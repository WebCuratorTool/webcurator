/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.common.ui.site;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.model.core.HasIdentity;

/**
 * @author bbeaumont
 */
public class EditorContext {

    /**
     * A cache of object that have unpersisted identity.
     */
    private Map<String, Map<String, HasIdentity>> objectCache = new HashMap<String, Map<String, HasIdentity>>();

    /**
     * Retrieve an object from the cache.
     *
     * @param clazz    The class type of the object.
     * @param identity The identifier of the object.
     * @return The object.
     */
    public HasIdentity getObject(Class clazz, String identity) {
        // Get the cache for the given object type.
        String key = WctUtils.getPrefixClassName(clazz);
        Map<String, HasIdentity> clazzCache = objectCache.get(key);

        // Get the object out of the cache.
        if (clazzCache != null) {
            return clazzCache.get(identity);
        } else {
            return null;
        }
    }

    /**
     * Used when the OID of the object is known.
     *
     * @param clazz    The class type of the object.
     * @param identity The identifier of the object.
     * @return The object.
     */
    public HasIdentity getObject(Class clazz, long identity) {
        return getObject(clazz, "" + identity);
    }

    /**
     * Puts an object into the cache.
     *
     * @param anObject The object to add to the cache.
     */
    public void putObject(HasIdentity anObject) {
        // Get the cache for the given object type.
        String key = WctUtils.getPrefixClassName(anObject.getClass());
        Map<String, HasIdentity> clazzCache = objectCache.get(key);

        // In case the object cache is empty.
        if (clazzCache == null) {
            clazzCache = new HashMap<String, HasIdentity>();
            objectCache.put(key, clazzCache);
        }

        // Make sure the object has a temporary identity?
        if (anObject.getIdentity() == null) {
            //anObject.setIdentity(getNewIdentity(anObject.getClass()));
        }

        // Add  to the cache.
        clazzCache.put(anObject.getIdentity(), anObject);
    }

    /**
     * Puts all of the collection's objects into the cache.
     *
     * @param aCollection A collection of objects to put into the cache.
     */
    public void putAllObjects(Collection<? extends HasIdentity> aCollection) {
        for (HasIdentity element : aCollection) {
            putObject(element);
        }
    }

    /**
     * Removes all of the collection's objects of class type.
     *
     * @param clazz The class type of the objects.
     */
    protected void removeObjectsOfType(Class clazz) {
        // Get the cache for the given object type.
        String key = WctUtils.getPrefixClassName(clazz);
        Map<String, HasIdentity> clazzCache = objectCache.get(key);

        // Get the object out of the cache.
        if (clazzCache != null) {
            clazzCache.clear();
        }
    }
}


