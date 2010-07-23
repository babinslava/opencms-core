/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsLinkWarningPanel.java,v $
 * Date   : $Date: 2010/07/23 11:38:25 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsSitemapBrokenLinkBean;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget containing that links to a sitemap item which the user wants to delete will be broken.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsLinkWarningPanel extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsLinkWarningPanelUiBinder extends UiBinder<Widget, CmsLinkWarningPanel> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsLinkWarningPanelUiBinder uiBinder = GWT.create(I_CmsLinkWarningPanelUiBinder.class);

    /** The label containing the warning that links will be broken. */
    @UiField
    protected Label m_label;

    /** The panel containing the links that will be broken. */
    @UiField
    protected CmsList<CmsTreeItem> m_linkPanel;

    /**
     * Default constructor.<p>
     */
    public CmsLinkWarningPanel() {

        initWidget(uiBinder.createAndBindUi(this));
        String text = Messages.get().key(Messages.GUI_BROKEN_LINK_TEXT_0);
        m_label.setText(text);
    }

    /**
     * Fills the panel with the tree list of broken links.<p>
     * 
     * @param brokenLinkBeans the beans representing the broken links 
     */
    public void fill(List<CmsSitemapBrokenLinkBean> brokenLinkBeans) {

        for (CmsSitemapBrokenLinkBean brokenLinkBean : brokenLinkBeans) {
            m_linkPanel.add(createTreeItem(brokenLinkBean));
        }
    }

    /**
     * Helper method for creating a list item widget based on a bean.<p>
     * 
     * @param brokenLinkBean the bean with the data for the list item widget 
     * 
     * @return the new list item widget
     */
    protected CmsListItemWidget createListItemWidget(CmsSitemapBrokenLinkBean brokenLinkBean) {

        CmsListInfoBean info = new CmsListInfoBean();
        String title = brokenLinkBean.getTitle();
        if ((title == null) || title.equals("")) {
            title = Messages.get().key(Messages.GUI_BROKEN_LINK_NO_TITLE_0);

        }
        info.setTitle(brokenLinkBean.getTitle());
        info.setSubTitle(brokenLinkBean.getSubTitle());
        CmsListItemWidget widget = new CmsListItemWidget(info);
        return widget;
    }

    /**
     * Helper method for creating a tree item from a bean.<p>
     * 
     * @param brokenLinkBean the bean containing the data for the tree item
     * 
     * @return a tree item 
     */
    protected CmsTreeItem createTreeItem(CmsSitemapBrokenLinkBean brokenLinkBean) {

        CmsListItemWidget itemWidget = createListItemWidget(brokenLinkBean);
        CmsTreeItem item = new CmsTreeItem(false, itemWidget);
        itemWidget.addTitleStyleName(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().deletedEntryLabel());
        itemWidget.addSubtitleStyleName(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().deletedEntryLabel());
        for (CmsSitemapBrokenLinkBean child : brokenLinkBean.getChildren()) {
            CmsListItemWidget childItemWidget = createListItemWidget(child);
            CmsTreeItem childItem = new CmsTreeItem(
                false,
                childItemWidget,
                I_CmsLayoutBundle.INSTANCE.sitemapItemCss().brokenLink());
            item.addChild(childItem);
        }
        return item;
    }

}
