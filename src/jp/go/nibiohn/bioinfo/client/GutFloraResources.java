package jp.go.nibiohn.bioinfo.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GutFloraResources extends ClientBundle {
	  @Source("glyphicons-43-pie-chart-s.png")
	  ImageResource getPieChartImageResource();

	  @Source("stacked-bar-chart-s.png")
	  ImageResource getBarChartImageResource();

	  @Source("heatmap-s.png")
	  ImageResource getHeatMapImageResource();
	  
	  @Source("pcoa-s.png")
	  ImageResource getPcoaImageResource();

	  @Source("ic_add_black_18dp_1x.png")
	  ImageResource getAddIconImageResource();

	  @Source("ic_remove_black_18dp_1x.png")
	  ImageResource getRemoveIconImageResource();

	  @Source("ic_settings_black_24dp_1x.png")
	  ImageResource getSettingIconImageResource();
	  
	  @Source("ic_menu_black_24dp.png")
	  ImageResource getMenuIconImageResource();

	  @Source("ic_storage_black_18dp.png")
	  ImageResource getStorageIconImageResource();
	  
	  @Source("ic_timeline_black_18dp.png")
	  ImageResource getTimelineIconImageResource();
	  
	  @Source("clustering.png")
	  ImageResource getClusteringImageResource();

	  @Source("glyphicons-369-collapse.png")
	  ImageResource getChevronRightImageResource();

	  @Source("glyphicons-368-expand.png")
	  ImageResource getChevronDownImageResource();
}
