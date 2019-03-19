/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.select.render;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.OrderByModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;

public class SelectRenderer {
    private static final String LIMIT_PARAMETER = "_limit"; //$NON-NLS-1$
    private static final String OFFSET_PARAMETER = "_offset"; //$NON-NLS-1$
    private SelectModel selectModel;
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence;
    
    private SelectRenderer(Builder builder) {
        selectModel = Objects.requireNonNull(builder.selectModel);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = builder.sequence.orElse(new AtomicInteger(1));
    }
    
    public SelectStatementProvider render() {
        QueryExpressionCollector collector = selectModel
                .mapQueryExpressions(this::renderQueryExpression)
                .collect(QueryExpressionCollector.collect());
        
        Map<String, Object> parameters = collector.parameters();
        Optional<String> limitClause = selectModel.limit().map(l -> renderLimit(parameters, l));
        Optional<String> offsetClause = selectModel.offset().map(o -> renderOffset(parameters, o));
        
        return DefaultSelectStatementProvider.withQueryExpression(collector.queryExpression())
                .withParameters(parameters)
                .withOrderByClause(selectModel.orderByModel().map(this::renderOrderBy))
                .withLimitClause(limitClause)
                .withOffsetClause(offsetClause)
                .build();
    }

    private QueryExpression renderQueryExpression(QueryExpressionModel queryExpressionModel) {
        return QueryExpressionRenderer.withQueryExpression(queryExpressionModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();
    }

    private String renderOrderBy(OrderByModel orderByModel) {
        return orderByModel.mapColumns(this::orderByPhrase)
                .collect(CustomCollectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private String orderByPhrase(SortSpecification column) {
        String phrase = column.aliasOrName();
        if (column.isDescending()) {
            phrase = phrase + " DESC"; //$NON-NLS-1$
        }
        return phrase;
    }
    
    private String renderLimit(Map<String, Object> parameters, Long limit) {
        BindableColumn<Integer> bc = SqlColumn.of(LIMIT_PARAMETER);
        String placeholder = renderingStrategy.getFormattedJdbcPlaceholder(bc, "parameters", LIMIT_PARAMETER); //$NON-NLS-1$ 
        parameters.put(LIMIT_PARAMETER, limit);
        return "limit " + placeholder; //$NON-NLS-1$
    }
    
    private String renderOffset(Map<String, Object> parameters, Long offset) {
        BindableColumn<Integer> bc = SqlColumn.of(OFFSET_PARAMETER);
        String placeholder = renderingStrategy.getFormattedJdbcPlaceholder(bc, "parameters", OFFSET_PARAMETER); //$NON-NLS-1$
        parameters.put(OFFSET_PARAMETER, offset);
        return "offset " + placeholder; //$NON-NLS-1$
    }
    
    public static Builder withSelectModel(SelectModel selectModel) {
        return new Builder().withSelectModel(selectModel);
    }
    
    public static class Builder {
        private SelectModel selectModel;
        private RenderingStrategy renderingStrategy;
        private Optional<AtomicInteger> sequence = Optional.empty();
        
        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = Optional.of(sequence);
            return this;
        }
        
        public SelectRenderer build() {
            return new SelectRenderer(this);
        }
    }
}
