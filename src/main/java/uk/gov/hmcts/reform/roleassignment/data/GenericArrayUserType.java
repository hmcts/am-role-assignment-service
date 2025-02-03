package uk.gov.hmcts.reform.roleassignment.data;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

public class GenericArrayUserType implements UserType<String[]> {

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public Class<String[]> returnedClass() {
        return String[].class;
    }

    @Override
    public boolean equals(String[] x, String[] y) {
        return Arrays.equals(x, y);
    }

    @Override
    public int hashCode(String[] x) {
        return Arrays.hashCode(x);
    }

    @Override
    public String[] nullSafeGet(ResultSet resultSet,
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
        return (String[]) array.getArray();
    }

    @Override
    public String[] deepCopy(String[] value) {
        return value;
    }

    @Override
    public void nullSafeSet(PreparedStatement statement,
                            String[] value,
                            int index,
                            SharedSessionContractImplementor session) throws SQLException {
        var connection = statement.getConnection();
        if (value == null) {
            statement.setNull(index, Types.ARRAY);
        } else {
            Array array = connection.createArrayOf("text", value);
            statement.setArray(index, array);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(String[] value) {
        return value;
    }

    @Override
    public String[] assemble(Serializable cached, Object owner) {
        return (String[]) cached;
    }
}
