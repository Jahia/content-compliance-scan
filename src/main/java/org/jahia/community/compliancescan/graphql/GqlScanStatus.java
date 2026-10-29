package org.jahia.community.compliancescan.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;

/**
 * GraphQL mirror of {@link org.jahia.community.compliancescan.model.ScanStatus}, named for the shared schema.
 */
@GraphQLName("ComplianceScanStatus")
@GraphQLDescription("Outcome of a compliance scan")
public enum GqlScanStatus {
    SAFE, ISSUES, NO_GLOSSARY
}
