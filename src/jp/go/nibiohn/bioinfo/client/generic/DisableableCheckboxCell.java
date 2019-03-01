package jp.go.nibiohn.bioinfo.client.generic;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Modified from GWT CheckboxCell class
 * 
 * @author chenyian
 *
 */
public class DisableableCheckboxCell extends AbstractEditableCell<Integer, Integer> {

	  private static final SafeHtml INPUT_CHECKED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked/>");

	  private static final SafeHtml INPUT_UNCHECKED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\"/>");

	  private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>");

	  private final boolean dependsOnSelection;
	  private final boolean handlesSelection;

	  /**
	   * Construct a new {@link CheckboxCell} that optionally controls selection.
	   *
	   * @param dependsOnSelection true if the cell depends on the selection state
	   * @param handlesSelection true if the cell modifies the selection state
	   */
	  public DisableableCheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
	    super(BrowserEvents.CHANGE, BrowserEvents.KEYDOWN);
	    this.dependsOnSelection = dependsOnSelection;
	    this.handlesSelection = handlesSelection;
	  }

	  @Override
	  public boolean dependsOnSelection() {
	    return dependsOnSelection;
	  }

	  @Override
	  public boolean handlesSelection() {
	    return handlesSelection;
	  }

	  @Override
	  public boolean isEditing(Context context, Element parent, Integer value) {
	    // A checkbox is never in "edit mode". There is no intermediate state
	    // between checked and unchecked.
	    return false;
	  }

	  @Override
	  public void onBrowserEvent(Context context, Element parent, Integer value, 
	      NativeEvent event, ValueUpdater<Integer> valueUpdater) {}

	  @Override
	  public void render(Context context, Integer value, SafeHtmlBuilder sb) {
	    // Get the view data.
	    Object key = context.getKey();
	    Integer viewData = getViewData(key);
	    if (viewData != null && viewData.equals(value)) {
	      clearViewData(key);
	      viewData = null;
	    }
	    
	    Integer relevantValue = viewData != null ? viewData : value;

		if (relevantValue.intValue() == 0) {
			sb.append(INPUT_UNCHECKED_DISABLED);
		} else if (relevantValue.intValue() > 0) {
			sb.append(INPUT_CHECKED);
		} else if (relevantValue.intValue() < 0) {
			sb.append(INPUT_UNCHECKED);
		}
	  }
}
