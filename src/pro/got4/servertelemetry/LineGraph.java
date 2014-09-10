/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pro.got4.servertelemetry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Average temperature demo chart.
 */
public class LineGraph extends AbstractChart {

	public Context context;
	public XYMultipleSeriesDataset dataset;
	public XYMultipleSeriesRenderer renderer;

	public String titleGraph;

	/**
	 * Returns the chart name.
	 * 
	 * @return the chart name
	 */
	public String getName() {

		return context.getResources().getString(R.string.chartName);
	}

	/**
	 * Returns the chart description.
	 * 
	 * @return the chart description
	 */
	public String getDesc() {
		return context.getResources().getString(R.string.chartDescription);
	}

	/**
	 * ��������� ���������� �������.
	 * 
	 * @param context
	 */
	public void adjust(Context context, String[] serieses, ArrayList<Date[]> x,
			ArrayList<double[]> y) {

		this.context = context;

		Resources resources = context.getResources();

		titleGraph = resources.getString(R.string.titleGraph);

		// ������ ������, ������������ ��� �����.
		int[] colors = new int[] { Color.RED };

		// ������ ������ �����, ������������ ��� �����.
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };

		// ��������� ������ ���������� �������������� ������������� ������� �
		// �������������� �����������.
		renderer = buildRenderer(colors, styles);

		// ����������� ����� �������.
		renderer.setXRoundedLabels(true);

		// ��������� ������� ������������� � ��������� ��� ��������.
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {

			XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) renderer
					.getSeriesRendererAt(i);

			seriesRenderer.setShowLegendItem(true);

			seriesRenderer.setLineWidth(3);

			seriesRenderer.setFillPoints(true);

			seriesRenderer.setDisplayChartValues(true);

			seriesRenderer.setChartValuesFormat(NumberFormat.getInstance());
			seriesRenderer.setChartValuesTextSize(25);

			// ���������� �� ����� �� �������.
			seriesRenderer.setChartValuesSpacing(10);

			// ���������� �� ����������� ����� ��������� ��������� � ������.
			seriesRenderer.setDisplayChartValuesDistance(80);
		}

		// ��������� ��������� �������� �������������:
		// - ����� ���������;
		// - ��������� ��� X;
		// - ��������� ��� Y;
		// - ����������� �������� �� ��� X;
		// - ������������ �������� �� ��� X;
		// - ����������� �������� �� ��� Y;
		// - ������������ �������� �� ��� Y;
		// - ���� ���� (Color.LTGRAY);
		// - ���� �������� � ���� (Color.LTGRAY).
		double xFirst = 0;
		double xLast = 12;
		if (x.size() > 0) {

			if (x.get(0).length > 0) {

				// �.�. ������� ������������� �� ����������� ���, �� �����
				// ������ ���� ���� ������.
				xFirst = x.get(0)[0].getTime();
				xLast = x.get(0)[x.get(0).length - 1].getTime();
			}
		}

		double yMin = -5;
		double yMax = 50;
		setChartSettings(renderer, resources.getString(R.string.titleGraph),
				resources.getString(R.string.titleX),
				resources.getString(R.string.titleY), xFirst, xLast, yMin,
				yMax, Color.LTGRAY, Color.LTGRAY);

		// ��������������� ���������� ������� �� ����.
		renderer.setXLabels(15);
		renderer.setYLabels(15);

		// ��������� �����.
		renderer.setShowGrid(true);

		// ������������ �������� ���� ������������ �������.
		renderer.setXLabelsAlign(Align.CENTER);
		renderer.setYLabelsAlign(Align.RIGHT);

		// ��������� ������ ��������.
		renderer.setZoomButtonsVisible(true);

		// ��������� �������� ��������������� ��� ������� �� ������� ��������.
		// public void setPanLimits(double[] panLimits)
		//
		// Sets the pan limits as an array of 4 values. Setting it to null or a
		// different size array will disable the panning limitation. Values:
		// [panMinimumX, panMaximumX, panMinimumY, panMaximumY]
		renderer.setPanLimits(new double[] { xFirst, xLast + 3600000 * 3, yMin,
				yMax });

		// ��������� �������� ���������� ��� ������� �� ������� ��������.
		// public void setZoomLimits(double[] zoomLimits)
		//
		// Sets the zoom limits as an array of 4 values. Setting it to null or a
		// different size array will disable the zooming limitation. Values:
		// [zoomMinimumX, zoomMaximumX, zoomMinimumY, zoomMaximumY]
		renderer.setZoomLimits(null);

		// To be set if the chart is inside a scroll view and doesn't need to
		// shrink when not enough space.
		// renderer.setInScroll(true);

		// ���������� ������ ������.
		dataset = buildDateDataset(serieses, x, y);

		// ��������� ����� �� ������� � ���������� ����������� � ����� ��
		// ��������� X � Y.
		int seriesCount = dataset.getSeriesCount();
		if (seriesCount > 0) {

			XYSeries series = dataset.getSeriesAt(0);

			int itemCount = series.getItemCount();
			if (itemCount > 0) {

				double x1 = series.getX(itemCount - 1);
				double y1 = series.getY(itemCount - 1);
				String annotation = String.format(Locale.getDefault(), "%.2f",
						y1);
				series.addAnnotation(annotation, x1, y1);
			}
		}
	}
}
