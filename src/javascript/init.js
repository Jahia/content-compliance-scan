import i18next from 'i18next';
import {registry} from '@jahia/ui-extender';
import {ComplianceScanAction} from './ComplianceScan/ComplianceScanAction';
import {NS} from './constants';
import en from '../main/resources/javascript/locales/en.json';
import fr from '../main/resources/javascript/locales/fr.json';

// The bundles ship with the code, so the labels exist before the action is registered.
function registerTranslations() {
    [['en', en], ['fr', fr]].forEach(([lang, resource]) => {
        if (!i18next.hasResourceBundle(lang, NS)) {
            i18next.addResourceBundle(lang, NS, resource[NS], true, true);
        }
    });
}

function register() {
    // A shield with a check mark.
    const buttonIcon = window.jahia?.moonstone?.toIconComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">' +
        '<path d="M12 1 3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/>' +
        '</svg>'
    );

    registry.add('action', 'contentComplianceScan', {
        targets: ['headerPrimaryActions:880'],
        buttonIcon,
        buttonLabel: `${NS}:action.open`,
        component: ComplianceScanAction
    });
}

export default function () {
    registerTranslations();
    register();
}
