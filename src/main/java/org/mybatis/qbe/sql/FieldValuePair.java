package org.mybatis.qbe.sql;

import org.mybatis.qbe.sql.where.SqlField;

public class FieldValuePair<T> {
    private T value;
    private SqlField<T> field;
    
    private FieldValuePair() {
        super();
    }
    
    public T getValue() {
        return value;
    }

    public SqlField<T> getField() {
        return field;
    }
    
    public FieldValuePair<T> ignoringAlias() {
        return FieldValuePair.of(field.ignoringAlias(), value);
    }
    
    public static <S> FieldValuePair<S> of(SqlField<S> field, S value) {
        FieldValuePair<S> phrase = new FieldValuePair<>();
        phrase.value = value;
        phrase.field = field;
        return phrase;
    }

    public static <S> FieldValuePair<S> of(SqlField<S> field) {
        FieldValuePair<S> phrase = new FieldValuePair<>();
        phrase.field = field;
        return phrase;
    }
}
