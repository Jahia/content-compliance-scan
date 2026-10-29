package org.jahia.community.compliancescan.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;

/**
 * GraphQL mirror of {@link org.jahia.community.compliancescan.model.IssueOrigin}, named for the shared schema.
 */
@GraphQLName("ComplianceIssueOrigin")
@GraphQLDescription("Where the content holding an issue comes from")
public enum GqlIssueOrigin {
    PAGE, REFERENCE, SHARED
}
