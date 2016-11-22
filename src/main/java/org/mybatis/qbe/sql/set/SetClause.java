package org.mybatis.qbe.sql.set;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.FieldValuePair;
import org.mybatis.qbe.sql.where.SqlField;

public class SetClause {
    private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();

    private SetClause(Stream<FieldValuePair<?>> fieldValuePairs) {
        fieldValuePairs.forEach(this.fieldValuePairs::add);
    }
    
    public void visitFieldValuePairs(Consumer<FieldValuePair<?>> consumer) {
        fieldValuePairs.stream().forEach(consumer);
    }
    
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private List<FieldValuePair<?>> fieldValuePairs = new ArrayList<>();
        
        public <S> AbstractBuilder(SqlField<S> field) {
            fieldValuePairs.add(FieldValuePair.of(field));
        }
        
        public <S> AbstractBuilder(SqlField<S> field, S value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
        }
        
        public <S> T set(SqlField<S> field, S value) {
            fieldValuePairs.add(FieldValuePair.of(field, value));
            return getThis();
        }
        
        public <S> T setNull(SqlField<S> field) {
            fieldValuePairs.add(FieldValuePair.of(field));
            return getThis();
        }
        
        public SetClause build() {
            return new SetClause(fieldValuePairs.stream());
        }
        
        public SetClause buildIgnoringAlias() {
            return new SetClause(fieldValuePairs.stream().map(FieldValuePair::ignoringAlias));
        }
        
        public abstract T getThis();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        public <T> Builder(SqlField<T> field) {
            super(field);
        }

        public <T> Builder(SqlField<T> field, T value) {
            super(field, value);
        }
        
        @Override
        public Builder getThis() {
            return this;
        }
    }
}
