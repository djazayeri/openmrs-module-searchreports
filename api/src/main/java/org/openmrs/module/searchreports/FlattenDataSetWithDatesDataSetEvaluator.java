package org.openmrs.module.searchreports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.DataSetRowComparator;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.SimpleDataSetMetaData;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Handler(supports = FlattenDataSetWithDatesDataSetDefinition.class)
public class FlattenDataSetWithDatesDataSetEvaluator implements DataSetEvaluator {
	
	private DataSetDefinitionService dataSetDefinitionService;
	
	@Autowired
	public FlattenDataSetWithDatesDataSetEvaluator(DataSetDefinitionService dataSetDefinitionService) {
		this.dataSetDefinitionService = dataSetDefinitionService;
	}
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext)
			throws EvaluationException {
		FlattenDataSetWithDatesDataSetDefinition def = (FlattenDataSetWithDatesDataSetDefinition) dataSetDefinition;
		DataSet input = dataSetDefinitionService.evaluate(def.getBaseDefinition(), evaluationContext);
		
		Map<Object, List<DataSetRow>> grouped = groupBy(input, def.getJoinOnColumn());
		
		SimpleDataSet output = new SimpleDataSet(def, evaluationContext);
		addCommonColumns(def, input, output);
		
		SortCriteria sortBy = new SortCriteria();
		sortBy.addSortElement(def.getSortByColumn(), SortCriteria.SortDirection.ASC);
		DataSetRowComparator comparator = new DataSetRowComparator(sortBy);
		int maxSize = 0;
		for (List<DataSetRow> groupedRows : grouped.values()) {
			maxSize = Math.max(maxSize, groupedRows.size());
			Collections.sort(groupedRows, comparator);
		}
		
		addRepeatedColumns(def, input, output, maxSize);
		
		addData(def, grouped, input.getMetaData().getColumns(), output);
		
		return output;
	}
	
	private void addData(FlattenDataSetWithDatesDataSetDefinition def, Map<Object, List<DataSetRow>> grouped,
	                     List<DataSetColumn> inputColumns, SimpleDataSet output) {
		SimpleDataSetMetaData metaData = output.getMetaData();
		DataSetColumn joinColumn = metaData.getColumn(def.getJoinOnColumn());
		for (Map.Entry<Object, List<DataSetRow>> entry : grouped.entrySet()) {
			DataSetRow outputRow = new DataSetRow();
			Object joinValue = entry.getKey();
			outputRow.addColumnValue(joinColumn, joinValue);
			List<DataSetRow> groupedRows = entry.getValue();
			for (int i = 0; i < groupedRows.size(); ++i) {
				String prefix = (i + 1) + "_";
				DataSetRow inputRow = groupedRows.get(i);
				for (DataSetColumn inputColumn : inputColumns) {
					if (inputColumn.equals(joinColumn)) {
						continue;
					}
					Object val = inputRow.getColumnValue(inputColumn);
					DataSetColumn col = metaData.getColumn(prefix + inputColumn.getName());
					outputRow.addColumnValue(col, val);
				}
			}
			output.addRow(outputRow);
		}
	}
	
	private void addRepeatedColumns(FlattenDataSetWithDatesDataSetDefinition def, DataSet input, SimpleDataSet output,
	                                int maxSize) {
		for (int i = 1; i <= maxSize; ++i) {
			String prefix = i + "_";
			for (DataSetColumn inCol : input.getMetaData().getColumns()) {
				if (!inCol.getName().equals(def.getJoinOnColumn())) {
					output.getMetaData().addColumn(
							new DataSetColumn(prefix + inCol.getName(), prefix + inCol.getLabel(), inCol.getDataType()));
				}
			}
		}
	}
	
	private void addCommonColumns(FlattenDataSetWithDatesDataSetDefinition def, DataSet input, SimpleDataSet output) {
		output.getMetaData().addColumn(input.getMetaData().getColumn(def.getJoinOnColumn()));
	}
	
	private Map<Object, List<DataSetRow>> groupBy(DataSet input, String joinOnColumn) {
		Map<Object, List<DataSetRow>> grouped = new LinkedHashMap<Object, List<DataSetRow>>();
		for (DataSetRow row : input) {
			Object joinOnValue = row.getColumnValue(joinOnColumn);
			List<DataSetRow> groupedRows = grouped.get(joinOnValue);
			if (groupedRows == null) {
				groupedRows = new ArrayList<DataSetRow>();
				grouped.put(joinOnValue, groupedRows);
			}
			groupedRows.add(row);
		}
		return grouped;
	}
}
