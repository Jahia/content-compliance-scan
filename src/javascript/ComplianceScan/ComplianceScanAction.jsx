import React, {useCallback, useState} from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import {useSelector} from 'react-redux';
import {useNodeChecks} from '@jahia/data-helper';
import {ComplianceScanDialog} from './ComplianceScanDialog';
import {NS} from '../constants';

export const ComplianceScanAction = ({path, language, render: Render, ...rest}) => {
    const [isOpen, setIsOpen] = useState(false);
    const reduxLanguage = useSelector(state => state.language);
    const jcontentPath = useSelector(state => state.jcontent?.path || null);
    const pagePath = path || jcontentPath;

    // The server checks the same permission; this only decides whether the button shows.
    const {checksResult} = useNodeChecks(
        {path: pagePath},
        {
            showOnNodeTypes: ['jnt:page'],
            requiredPermission: ['jcr:write'],
            requireModuleInstalledOnSite: [NS]
        }
    );
    const close = useCallback(() => setIsOpen(false), []);

    if (!pagePath || !checksResult) {
        return null;
    }

    return (
        <>
            <Render {...rest} onClick={() => setIsOpen(true)}/>
            {isOpen && ReactDOM.createPortal(
                <ComplianceScanDialog
                    path={pagePath}
                    language={language || reduxLanguage}
                    onReopen={() => setIsOpen(true)}
                    onClose={close}
                />,
                document.body
            )}
        </>
    );
};

ComplianceScanAction.propTypes = {
    path: PropTypes.string,
    language: PropTypes.string,
    render: PropTypes.elementType.isRequired
};
