package jp.go.nibiohn.bioinfo.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ItemSelectionWidget extends Composite {
	
	private static String ARROW_BUTTON_WIDTH = "34px";
	
	private ListBox availableListBox = new ListBox();

	private ListBox selectedListBox = new ListBox();
	
	protected SimplePanel availableHeadPanel = new SimplePanel();
	
	protected SimplePanel selectedHeadPanel = new SimplePanel();
	
	protected List<String> availableItems;
	
	// TODO what if user want unlimited number of items?
	protected int maxiumSelectableItems = 11;

	public ItemSelectionWidget(List<String> availableItems, List<String> currentSelectedItems) {
		this.availableItems = availableItems;
		
		VerticalPanel thisWidget = new VerticalPanel();
		
		HorizontalPanel selectionHp = new HorizontalPanel();
		selectionHp.setSpacing(6);
		selectionHp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		VerticalPanel availableVp = new VerticalPanel();
		availableHeadPanel.add(new HTML("<b>Available Items:</b>"));
		availableVp.add(availableHeadPanel);
		availableVp.add(availableListBox);
		availableListBox.setMultipleSelect(true);
		availableListBox.setVisibleItemCount(10);
		availableListBox.setWidth("200px");
		
		VerticalPanel selectedVp = new VerticalPanel();
		selectedHeadPanel.add(new HTML("<b>Selected Items:</b>"));
		selectedVp.add(selectedHeadPanel);
		selectedVp.add(selectedListBox);
		selectedListBox.setMultipleSelect(false);
		selectedListBox.setVisibleItemCount(10);
		selectedListBox.setWidth("200px");
		for (String itemText : currentSelectedItems) {
			selectedListBox.addItem(itemText);
		}
		
		VerticalPanel leftRightVp = new VerticalPanel();
		// 'arrow_forward' or 'chevron_right'
		Button rightBtn = new Button("<i class=\"material-icons md-18\">arrow_forward</i>");
		rightBtn.setWidth(ARROW_BUTTON_WIDTH);
		rightBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (selectedListBox.getItemCount() < maxiumSelectableItems) {
					int index = availableListBox.getSelectedIndex();
					if (index != -1) {
						selectedListBox.addItem(availableListBox.getValue(index));
						updateAvailableList();
						if (availableListBox.getItemCount() > index) {
							availableListBox.setSelectedIndex(index);
						} else {
							availableListBox.setSelectedIndex(availableListBox.getItemCount() - 1);
						}
					}
				}
			}
		});
		leftRightVp.add(rightBtn);
		// 'arrow_back' or 'chevron_left'
		Button leftBtn = new Button("<i class=\"material-icons md-18\">arrow_back</i>");
		leftBtn.setWidth(ARROW_BUTTON_WIDTH);
		leftBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int index = selectedListBox.getSelectedIndex();
				if (index != -1) {
					selectedListBox.removeItem(index);
					updateAvailableList();
					if (selectedListBox.getItemCount() > index) {
						selectedListBox.setSelectedIndex(index);
					} else {
						selectedListBox.setSelectedIndex(selectedListBox.getItemCount() - 1);
					}
				}
			}
		});
		leftRightVp.add(leftBtn);

		VerticalPanel upDownVp = new VerticalPanel();
		// 'arrow_upward' or 'expand_less' 
		Button upBtn = new Button("<i class=\"material-icons md-18\">arrow_upward</i>");
		upBtn.setWidth(ARROW_BUTTON_WIDTH);
		upBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int index = selectedListBox.getSelectedIndex();
				if (index > 0) {
					String value = selectedListBox.getValue(index);
					selectedListBox.removeItem(index);
					selectedListBox.insertItem(value, index - 1);
					selectedListBox.setItemSelected(index - 1, true);
				}
			}
		});
		upDownVp.add(upBtn);
		// 'arrow_downward' or 'expand_more'
		Button downBtn = new Button("<i class=\"material-icons md-18\">arrow_downward</i>");
		downBtn.setWidth(ARROW_BUTTON_WIDTH);
		downBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int index = selectedListBox.getSelectedIndex();
				if (index < selectedListBox.getItemCount() - 1) {
					String value = selectedListBox.getValue(index);
					selectedListBox.removeItem(index);
					selectedListBox.insertItem(value, index + 1);
					selectedListBox.setItemSelected(index + 1, true);
				}
			}
		});
		upDownVp.add(downBtn);
		
		selectionHp.add(availableVp);
		selectionHp.add(leftRightVp);
		selectionHp.add(selectedVp);
		selectionHp.add(upDownVp);
		
		thisWidget.add(selectionHp);

		updateAvailableList();
		
		initWidget(thisWidget);
	}
	
	private void updateAvailableList() {
		Set<String> selected = new HashSet<String>();
		for (int i = 0; i < selectedListBox.getItemCount(); i++) {
			selected.add(selectedListBox.getValue(i));
		}
		availableListBox.clear();
		for (String itemText : availableItems) {
			if (!selected.contains(itemText)) {
				availableListBox.addItem(itemText);
			}
		}
	}

	public List<String> getSelectedItems() {
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < selectedListBox.getItemCount(); i++) {
			ret.add(selectedListBox.getValue(i));
		}
		return ret;
	}

	// for customizations
	
	public ItemSelectionWidget setAvailableItemHead(Widget widget) {
		availableHeadPanel.setWidget(widget);
		return this;
	}
	
	public ItemSelectionWidget setSelectedItemHead(Widget widget) {
		selectedHeadPanel.setWidget(widget);
		return this;
	}

	public ItemSelectionWidget setAvailableListBoxWidth(String width) {
		availableListBox.setWidth(width);
		return this;
	}

	public ItemSelectionWidget setSelectedListBoxWidth(String width) {
		selectedListBox.setWidth(width);
		return this;
	}

	public ItemSelectionWidget setMaxiumSelectableItems(int maxiumSelectableItems) {
		this.maxiumSelectableItems = maxiumSelectableItems;
		return this;
	}

	public void resetSelectedColumns() {
		resetSelectedColumns(maxiumSelectableItems);
	}

	public void resetSelectedColumns(int defaultNum) {
		selectedListBox.clear();
		for (int i = 0; i < availableItems.size() && i < defaultNum; i++) {
			selectedListBox.addItem(availableItems.get(i));
		}
		updateAvailableList();
	}
	
}
