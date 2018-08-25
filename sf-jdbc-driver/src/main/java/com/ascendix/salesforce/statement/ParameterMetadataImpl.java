package com.ascendix.salesforce.statement;

import org.apache.commons.lang3.StringUtils;

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterMetadataImpl implements ParameterMetaData {

    private List<Object> parameters = new ArrayList<>();

    public ParameterMetadataImpl(List<Object> parameters, String query) {
        super();
        this.parameters.addAll(parameters);
        int paramsCountInQuery = StringUtils.countMatches(query, '?');
        if (this.parameters.size() < paramsCountInQuery) {
            this.parameters.addAll(Collections.nCopies(paramsCountInQuery - this.parameters.size(), new Object()));
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getParameterCount() throws SQLException {
        return parameters.size();
    }

    @Override
    public int isNullable(int param) throws SQLException {
        return ParameterMetaData.parameterNullable;
    }

    @Override
    public boolean isSigned(int param) throws SQLException {
        return parameters.get(param + 1).getClass().isInstance(Number.class);
    }

    @Override
    public int getPrecision(int param) throws SQLException {
        return 0;
    }

    @Override
    public int getScale(int param) throws SQLException {
        return 0;
    }

    @Override
    public int getParameterType(int param) throws SQLException {
        return Types.NVARCHAR;
    }

    @Override
    public String getParameterTypeName(int param) throws SQLException {
        return "varchar";
    }

    @Override
    public String getParameterClassName(int param) throws SQLException {
        return parameters.get(param + 1).getClass().getName();
    }

    @Override
    public int getParameterMode(int param) throws SQLException {
        return ParameterMetaData.parameterModeIn;
    }

}
