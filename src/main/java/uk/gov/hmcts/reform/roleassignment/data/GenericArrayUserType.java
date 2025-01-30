package uk.gov.hmcts.reform.roleassignment.data;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class GenericArrayUserType implements UserType<Object> {

    private Class<Object> typeParameterClass;

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public Class<Object> returnedClass() {
        return typeParameterClass;
    }

    @Override
    public boolean equals(Object x, Object y) {
        if (x == null) {
            return y == null;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(Object x) {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet,
                              int position,
                              SharedSessionContractImplementor session,
                              Object owner) throws SQLException {
        if (resultSet.wasNull()) {
            return null;
        }
        Array array = resultSet.getArray(position);
        if (array == null) {
            return new String[0];
        }
        return  array.getArray();
    }

    @Override
    public void nullSafeSet(PreparedStatement statement,
                            Object value,
                            int index,
                            SharedSessionContractImplementor session) throws SQLException {
        var connection = statement.getConnection();
        if (value == null) {
            statement.setNull(index, Types.ARRAY);
        } else {
            var array = connection.createArrayOf("text", (Object[]) value);
            statement.setArray(index, array);
        }
    }

    @Override
    public Object deepCopy(Object value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }
}
