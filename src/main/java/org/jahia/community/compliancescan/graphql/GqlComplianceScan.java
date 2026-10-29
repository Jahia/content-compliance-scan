package org.jahia.community.compliancescan.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.jahia.community.compliancescan.service.spi.ComplianceScanService;
import org.jahia.modules.graphql.provider.dxm.DataFetchingException;
import org.jahia.modules.graphql.provider.dxm.osgi.annotations.GraphQLOsgiService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Compliance scan queries. Read-only: nothing is written, so the CSRF guard does not apply.
 */
@GraphQLName("ComplianceScanQuery")
@GraphQLDescription("Content compliance scan queries")
public class GqlComplianceScan {

    private static final Logger logger = LoggerFactory.getLogger(GqlComplianceScan.class);

    /** Who may scan a page: whoever may edit it. Checked on the page in the default workspace. */
    static final String REQUIRED_PERMISSION = "jcr:write";
    /** The scan runs only on sites that enable this module, as the Page Builder button does. */
    static final String MODULE_ID = "content-compliance-scan";
    private static final String EDIT_WORKSPACE = "default";
    private static final String PAGE_TYPE = "jnt:page";
    private static final Pattern LANGUAGE = Pattern.compile("^[A-Za-z]{2,3}([_-][A-Za-z0-9]{2,8}){0,3}$");
    private static final String DENIED = "Permission denied";

    @Inject
    @GraphQLOsgiService
    private ComplianceScanService scanService;

    /**
     * Scans a page in one language of the default workspace, as the current user.
     *
     * @param path          path of the page
     * @param language      content language to scan
     * @param uiLanguage    language of the labels in the report, English when missing
     * @param renderedPaths paths of the contents the edit frame shows for the page
     * @return the report
     */
    @GraphQLField
    @GraphQLName("scanPage")
    @GraphQLNonNull
    @GraphQLDescription("Scans the text of a page, of the content under it, of the content it references and of the shared content it shows")
    public GqlComplianceReport scanPage(
            @GraphQLName("path") @GraphQLNonNull @GraphQLDescription("Path of the page") String path,
            @GraphQLName("language") @GraphQLNonNull @GraphQLDescription("Content language to scan") String language,
            @GraphQLName("uiLanguage") @GraphQLDescription("Language of the labels in the report") String uiLanguage,
            @GraphQLName("renderedPaths") @GraphQLDescription("Paths of the contents the edit frame shows for this page (header, footer, listings)") List<String> renderedPaths) {
        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user == null || JahiaUserManagerService.isGuest(user)) {
            throw new DataFetchingException(DENIED);
        }
        if (language == null || !LANGUAGE.matcher(language).matches()) {
            throw new DataFetchingException("Invalid language");
        }
        Locale contentLocale = LanguageCodeConverters.languageCodeToLocale(language);
        Locale uiLocale = uiLanguage != null && LANGUAGE.matcher(uiLanguage).matches()
                ? LanguageCodeConverters.languageCodeToLocale(uiLanguage) : Locale.ENGLISH;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(EDIT_WORKSPACE, contentLocale);
            JCRNodeWrapper page = session.getNode(path);
            if (!page.isNodeType(PAGE_TYPE) || !page.hasPermission(REQUIRED_PERMISSION)) {
                throw new DataFetchingException(DENIED);
            }
            if (page.getResolveSite() == null || !page.getResolveSite().getInstalledModules().contains(MODULE_ID)) {
                throw new DataFetchingException("The compliance scan is not enabled on this site");
            }
            return new GqlComplianceReport(scanService.scan(page, uiLocale, renderedPaths));
        } catch (PathNotFoundException e) {
            throw new DataFetchingException(DENIED);
        } catch (RepositoryException e) {
            logger.warn("Compliance scan of {} in {} failed: {}", path, language, e.toString());
            throw new DataFetchingException("The scan could not read the content");
        }
    }
}
