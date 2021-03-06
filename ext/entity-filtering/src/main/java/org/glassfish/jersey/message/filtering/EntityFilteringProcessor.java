/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.jersey.message.filtering;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityProcessorContext;

import com.google.common.collect.Sets;

/**
 * Entity processor handling entity-filtering annotations.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
@Singleton
@Priority(Integer.MAX_VALUE - 2000)
final class EntityFilteringProcessor extends AbstractEntityProcessor {

    @Override
    public Result process(final EntityProcessorContext context) {
        switch (context.getType()) {
            case CLASS_READER:
            case CLASS_WRITER:
                addGlobalScopes(EntityFilteringHelper.getFilteringScopes(context.getEntityClass().getDeclaredAnnotations()),
                        context.getEntityGraph());
                break;
        }
        return super.process(context);
    }

    @Override
    protected Result process(final String field, final Class<?> fieldClass, final Annotation[] fieldAnnotations,
                             final Annotation[] annotations, final EntityGraph graph) {
        Set<String> filteringScopes = Sets.newHashSet();

        if (fieldAnnotations.length > 0) {
            filteringScopes.addAll(EntityFilteringHelper.getFilteringScopes(fieldAnnotations));
        }
        if (annotations.length > 0) {
            filteringScopes.addAll(EntityFilteringHelper.getFilteringScopes(annotations));
        }

        if (!filteringScopes.isEmpty()) {
            if (field != null) {
                addFilteringScopes(field, fieldClass, filteringScopes, graph);
            } else {
                addGlobalScopes(filteringScopes, graph);
            }
            return Result.APPLY;
        } else {
            return Result.SKIP;
        }
    }
}
