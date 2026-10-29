package org.jahia.community.compliancescan.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.modules.graphql.provider.dxm.DXGraphQLProvider;

/**
 * Adds {@code complianceScan { ... }} to the root Query.
 */
@GraphQLTypeExtension(DXGraphQLProvider.Query.class)
public final class ComplianceScanQueryExtension {

    private ComplianceScanQueryExtension() {
    }

    /**
     * @return the entry point of the compliance scan queries
     */
    @GraphQLField
    @GraphQLName("complianceScan")
    @GraphQLNonNull
    @GraphQLDescription("Content compliance scan: checks content against the site's glossaries of non-authorised terms")
    public static GqlComplianceScan complianceScan() {
        return new GqlComplianceScan();
    }
}
