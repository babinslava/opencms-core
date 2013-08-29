/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.client.CmsPublishItemStatus.Signal;
import org.opencms.ade.publish.client.CmsPublishSelectPanel.CheckBoxUpdate;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPreviewDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsTriStateCheckBox;
import org.opencms.gwt.client.ui.input.CmsTriStateCheckBox.State;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.util.CmsResourceStateUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel representing a single publish group.<p>
 * 
 * @since 8.0.0
 */
public class CmsPublishGroupPanel extends Composite {

    /** The CSS bundle used for this widget. */
    protected static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();

    /** The number of button slits. */
    private static final int NUM_BUTTON_SLOTS = 3;

    /** The slot for the preview button. */
    private static final int SLOT_PREVIEW = 0;

    /** The slot for the 'remove' checkbox. */
    private static final int SLOT_REMOVE = 1;

    /** The slot for the warning symbol. */
    private static final int SLOT_WARNING = 2;

    /** Text metrics key. */
    private static final String TM_PUBLISH_LIST = "PublishList";

    /** The group index for this panel's corresponding group. */
    protected int m_groupIndex;

    /** The data model for the publish dialog. */
    protected CmsPublishDataModel m_model;

    /** The handler which is called when the publish item selection changes. */
    protected I_CmsPublishSelectionChangeHandler m_selectionChangeHandler;

    /** The global map of selection controllers of *ALL* groups (to which this group's selection controllers are added). */
    private Map<CmsUUID, CmsPublishItemSelectionController> m_controllersById;

    /** The group header (containing the label and add/remove buttons). */
    private CmsSimpleListItem m_header = new CmsSimpleListItem();

    /** The number of loaded publish item widgets for this group (used for scrolling).<p> */
    private int m_itemIndex;

    /** The root panel of this widget. */
    private CmsList<CmsTreeItem> m_panel = new CmsList<CmsTreeItem>();

    /** The publish resources of the current group.<p>*/
    private List<CmsPublishResource> m_publishResources;

    /** Checkbox for selecting/deselecting all group items. */
    private CmsTriStateCheckBox m_selectGroup;

    /** A flag which indicates whether only resources with problems should be shown. */
    private boolean m_showProblemsOnly;

    /**
     * Constructs a new instance.<p>
     * 
     * @param title the title of the group
     * @param groupIndex the index of the group which this panel should render
     * @param selectionChangeHandler the handler for selection changes for publish resources
     * @param model the data model for the publish resources
     * @param controllersById the map of selection controllers to which this panel's selection controllers should be added
     * @param showProblemsOnly if true, sets this panel into "show resources with problems only" mode
     */
    public CmsPublishGroupPanel(
        String title,
        int groupIndex,
        I_CmsPublishSelectionChangeHandler selectionChangeHandler,
        CmsPublishDataModel model,
        Map<CmsUUID, CmsPublishItemSelectionController> controllersById,
        boolean showProblemsOnly) {

        initWidget(m_panel);
        m_panel.add(m_header);
        m_model = model;
        m_groupIndex = groupIndex;
        m_publishResources = model.getGroups().get(groupIndex).getResources();
        m_controllersById = controllersById;
        m_panel.truncate(TM_PUBLISH_LIST, CmsPublishDialog.DIALOG_WIDTH);
        initSelectButtons();
        if (groupIndex == 0) {
            m_model.signalGroup(Signal.publish, 0);
        }
        m_showProblemsOnly = showProblemsOnly;
        if (hasNoProblemResources() && showProblemsOnly) {
            this.setVisible(false);
        }

        HTML label = new HTML();
        label.setHTML(title + CmsPublishSelectPanel.formatResourceCount(m_publishResources.size()));
        label.addStyleName(CSS.groupHeader());
        m_header.add(label);

        FlowPanel clear = new FlowPanel();
        clear.setStyleName(CSS.clear());
        m_header.add(clear);
        m_selectionChangeHandler = selectionChangeHandler;
    }

    /**
     * Creates a basic list item widget for a given publish resource bean.<p>
     * 
     * @param resourceBean the publish resource bean
     * 
     * @return the list item widget representing the publish resource bean 
     */
    public static CmsListItemWidget createListItemWidget(final CmsPublishResource resourceBean) {

        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(getTitle(resourceBean));
        info.setSubTitle(resourceBean.getName());
        String stateLabel = org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_RESOURCE_STATE_0);
        String stateName = CmsResourceStateUtil.getStateName(resourceBean.getState());
        // this can be null for the source resources of broken relations in the 'broken links' 
        // panel since these resources don't have to be new or deleted or changed
        if (stateName != null) {
            info.addAdditionalInfo(stateLabel, stateName, CmsResourceStateUtil.getStateStyle(resourceBean.getState()));
        }
        if (resourceBean.getUserLastModified() != null) {
            String userLabel = org.opencms.ade.publish.client.Messages.get().key(
                org.opencms.ade.publish.client.Messages.GUI_LABEL_USER_LAST_MODIFIED_0);
            info.addAdditionalInfo(userLabel, resourceBean.getUserLastModified());
        }
        if (resourceBean.getDateLastModifiedString() != null) {
            String dateLabel = org.opencms.ade.publish.client.Messages.get().key(
                org.opencms.ade.publish.client.Messages.GUI_LABEL_DATE_LAST_MODIFIED_0);
            info.addAdditionalInfo(dateLabel, resourceBean.getDateLastModifiedString());
        }

        CmsListItemWidget itemWidget = new CmsListItemWidget(info);
        for (int i = 0; i < NUM_BUTTON_SLOTS; i++) {
            SimplePanel panel = new SimplePanel();
            panel.getElement().getStyle().setWidth(20, Unit.PX);
            panel.getElement().getStyle().setHeight(20, Unit.PX);
            if (i == SLOT_WARNING) {
                panel.addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
            }
            itemWidget.addButton(panel);
        }

        if (CmsPublishDataModel.hasProblems(resourceBean)) {
            Image warningImage = new Image(I_CmsImageBundle.INSTANCE.warningSmallImage());
            warningImage.setTitle(resourceBean.getInfo().getValue());
            warningImage.addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
            fillButtonSlot(itemWidget, SLOT_WARNING, warningImage);
        }
        String noPreviewReason = resourceBean.getInfo() == null ? null : resourceBean.getInfo().getNoPreviewReason();
        CmsPushButton previewButton = new CmsPushButton();
        previewButton.setImageClass(I_CmsImageBundle.INSTANCE.style().previewIcon());
        previewButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        previewButton.setTitle(org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_SHOW_PREVIEW_0));
        previewButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsPushButton button = (CmsPushButton)event.getSource();
                button.clearHoverState();
                CmsPreviewDialog.showPreviewForResource(resourceBean.getId());
            }
        });
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(noPreviewReason)) {
            previewButton.disable(noPreviewReason);
        }
        fillButtonSlot(itemWidget, SLOT_PREVIEW, previewButton);
        itemWidget.setUnselectable();
        itemWidget.setIcon(CmsIconUtil.getResourceIconClasses(resourceBean.getResourceType(), false));
        return itemWidget;
    }

    /**
     * Fills a slot for a button in a publish list item widget.<p>
     *  
     * @param listItemWidget the list item widget 
     * @param index the slot index 
     * @param widget the widget which should be displayed in the slot 
     */
    private static void fillButtonSlot(CmsListItemWidget listItemWidget, int index, Widget widget) {

        SimplePanel panel = (SimplePanel)listItemWidget.getButton(index);
        panel.clear();
        panel.add(widget);
    }

    /** 
     * Utility method for getting the title of a publish resource bean, or a default title 
     * if the bean has no title.<p>
     * 
     * @param resourceBean the resource bean for which the title should be retrieved
     *  
     * @return the bean's title, or a default title
     */
    private static String getTitle(CmsPublishResource resourceBean) {

        String title = resourceBean.getTitle();
        if ((title == null) || title.equals("")) {
            title = Messages.get().key(Messages.GUI_NO_TITLE_0);
        }
        return title;
    }

    /**
     * Adds the list item for the next publish resource and returns  true on success, while
     * also incrementing the internal item index.<p>
     * 
     * @return true if an item was added
     */
    public boolean addNextItem() {

        if (m_itemIndex >= m_publishResources.size()) {
            return false;
        }
        CmsPublishResource res = m_publishResources.get(m_itemIndex);
        m_itemIndex += 1;
        if (m_showProblemsOnly && (!CmsPublishDataModel.hasProblems(res))) {
            return false;
        } else {
            addItem(res);
            return true;
        }
    }

    /**
     * Returns true if there are more potential items to add.<p>
     * 
     * @return true if there are possibly more items 
     */
    public boolean hasMoreItems() {

        return m_itemIndex < m_publishResources.size();
    }

    /**
     * Hides the tri-state select box for the group.<p>
     */
    public void hideGroupSelectCheckBox() {

        m_selectGroup.getElement().getStyle().setVisibility(Visibility.HIDDEN);
    }

    /** 
     * Updates the check box state for this group.<p>
     * 
     * @param value the state to use for updating the check box 
     */
    public void updateCheckboxState(CmsPublishItemStateSummary value) {

        CheckBoxUpdate update = CmsPublishSelectPanel.updateCheckbox(value);
        m_selectGroup.setTitle(update.getAction());
        m_selectGroup.setState(update.getState(), false);
    }

    /**
     * Returns true if the corresponding group has no  resources with problems.<p>
     * 
     * @return true if the group for this panel has no resources with problems 
     */
    protected boolean hasNoProblemResources() {

        return 0 == m_model.countResourcesInGroup(
            new CmsPublishDataModel.HasProblems(),
            m_model.getGroups().get(m_groupIndex).getResources());
    }

    /**
     * Returns true if the corresponding group has only resources with problems.<p>
     * 
     * @return true if the group for this panel has only resources with problems. 
     */
    protected boolean hasOnlyProblemResources() {

        return m_model.getGroups().get(m_groupIndex).getResources().size() == m_model.countResourcesInGroup(
            new CmsPublishDataModel.HasProblems(),
            m_model.getGroups().get(m_groupIndex).getResources());
    }

    /**
     * Adds a resource bean to this group.<p>
     * 
     * @param resourceBean the resource bean which should be added
     */
    private void addItem(CmsPublishResource resourceBean) {

        CmsTreeItem row = buildItem(resourceBean, m_model.getStatus(resourceBean.getId()), false);
        m_panel.add(row);

        for (CmsPublishResource related : resourceBean.getRelated()) {
            row.addChild(buildItem(related, m_model.getStatus(related.getId()), true));
        }
    }

    /**
     * Creates a widget from resource bean data.<p>
     * 
     * @param resourceBean the resource bean for which a widget should be constructed
     * @param status the publish item status
     * @param isSubItem true if this is not a top-level publish item  
     * 
     * @return a widget representing the resource bean
     */
    private CmsTreeItem buildItem(final CmsPublishResource resourceBean, CmsPublishItemStatus status, boolean isSubItem) {

        CmsListItemWidget itemWidget = createListItemWidget(resourceBean);
        final CmsStyleVariable styleVar = new CmsStyleVariable(itemWidget);
        styleVar.setValue(CSS.itemToKeep());

        final CmsCheckBox checkbox = new CmsCheckBox();
        CmsTreeItem row;
        row = new CmsTreeItem(true, checkbox, itemWidget);
        if (isSubItem) {
            checkbox.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }

        row.setOpen(false);
        row.addStyleName(CSS.publishRow());

        // we do not need most of the interactive elements for the sub-items 
        if (!isSubItem) {
            ClickHandler checkboxHandler = new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    boolean checked = checkbox.isChecked();
                    m_model.signal(checked ? Signal.publish : Signal.unpublish, resourceBean.getId());
                }
            };
            checkbox.addClickHandler(checkboxHandler);

            final boolean hasProblem = CmsPublishDataModel.hasProblems(resourceBean);
            if (hasProblem) {
                // can't select resource with problems
                checkbox.setChecked(false);
                checkbox.setEnabled(false);
            }

            final CmsCheckBox remover = new CmsCheckBox();
            final CmsPublishItemSelectionController controller = new CmsPublishItemSelectionController(
                resourceBean.getId(),
                checkbox,
                remover,
                styleVar,
                hasProblem);
            m_controllersById.put(resourceBean.getId(), controller);

            remover.setTitle(Messages.get().key(Messages.GUI_PUBLISH_REMOVE_BUTTON_0));
            remover.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent e) {

                    boolean remove = remover.isChecked();
                    m_model.signal(remove ? Signal.remove : Signal.unremove, resourceBean.getId());
                }
            });
            if (resourceBean.isRemovable()) {
                fillButtonSlot(itemWidget, SLOT_REMOVE, remover);
            }
            controller.update(status);
        }
        return row;
    }

    /**
     * Initializes the "select all/none" buttons, adds them to the group header and 
     * attaches event handlers to them.<p>
     */
    private void initSelectButtons() {

        m_selectGroup = new CmsTriStateCheckBox("");
        m_selectGroup.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().inlineBlock());
        m_selectGroup.setNextStateAfterIntermediateState(State.on);
        m_selectGroup.addValueChangeHandler(new ValueChangeHandler<CmsTriStateCheckBox.State>() {

            public void onValueChange(ValueChangeEvent<State> event) {

                State state = event.getValue();
                if (state == State.on) {
                    m_model.signalGroup(Signal.publish, m_groupIndex);
                } else if (state == State.off) {
                    m_model.signalGroup(Signal.unpublish, m_groupIndex);
                }
            }

        });
        m_header.add(m_selectGroup);
    }
}
