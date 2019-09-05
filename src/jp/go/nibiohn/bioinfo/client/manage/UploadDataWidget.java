package jp.go.nibiohn.bioinfo.client.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;

import jp.go.nibiohn.bioinfo.client.GutFloraService;
import jp.go.nibiohn.bioinfo.client.GutFloraServiceAsync;

public class UploadDataWidget extends Composite {
	
	protected static Logger rootLogger = Logger.getLogger("");
	
	protected final GutFloraServiceAsync service = GWT.create(GutFloraService.class);

	public UploadDataWidget(String tableName) {
		VerticalPanel thisWidget = new VerticalPanel();
		
		HorizontalPanel fileUploadHp = new HorizontalPanel();
		FileUpload fileUpload = new FileUpload();
		Button uploadBtn = new Button("Upload");
		fileUploadHp.add(fileUpload);
		fileUploadHp.add(uploadBtn);
		thisWidget.add(fileUploadHp);
		
		CellTable<List<String>> table = createTable();
		
		thisWidget.add(table);
		
		initWidget(thisWidget);
	}

	private CellTable<List<String>> createTable() {
		CellTable<List<String>> ret = new CellTable<List<String>>();
		ret.setSelectionModel(new NoSelectionModel<List<String>>());
		ret.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

		List<List<String>> dummy = new ArrayList<List<String>>();
		dummy.add(Arrays.asList("A1", "A2"));
		dummy.add(Arrays.asList("B1", "B2"));
		dummy.add(Arrays.asList("C1", "C2"));
		
		ListDataProvider<List<String>> dataProvider = new ListDataProvider<List<String>>(dummy);
		List<String> columns = Arrays.asList("column1","column2");
		for (int i = 0; i < columns.size(); i++) {
			final Integer[] index = new Integer[] {i};
			TextColumn<List<String>> column = new TextColumn<List<String>>() {
				
				@Override
				public String getValue(List<String> object) {
					return object.get(index[0]);
				}
			};
			ret.addColumn(column, columns.get(i));
		}
		dataProvider.addDataDisplay(ret);
		return ret;
	}

	

}
