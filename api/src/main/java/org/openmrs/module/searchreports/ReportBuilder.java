package org.openmrs.module.searchreports;

import org.openmrs.api.ConceptService;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDatetimeDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.ObsForEncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.query.encounter.definition.ObsForEncounterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportBuilder {
	
	@Autowired
	private ConceptService conceptService;
	
	public DataSetDefinition buildRowPerEncounterDataSetDefinition() {
		PropertyConverter valueNumeric = new PropertyConverter(Integer.class, "valueNumeric");
		PropertyConverter valueCodedId = new PropertyConverter(Integer.class, "valueCoded.conceptId");
		
		DateConverter ymd = new DateConverter("yyyy-MM-dd");
		
		ObsForEncounterQuery hasRequiredObs = new ObsForEncounterQuery();
		hasRequiredObs.setQuestion(conceptService.getConcept(5089));
		
		EncounterDataSetDefinition dsd = new EncounterDataSetDefinition();
		dsd.addRowFilter(hasRequiredObs, null);
		dsd.addColumn("patient_id", new PatientIdDataDefinition(), null);
		dsd.addColumn("encounter_datetime", new EncounterDatetimeDataDefinition(), null, ymd);
		dsd.addColumn("wt", colHelper(5089), null, valueNumeric);
		dsd.addColumn("cd4", colHelper(5497), null, valueNumeric);
		dsd.addColumn("food", colHelper(21), null, valueCodedId);
		
		return dsd;
	}
	
	public DataSetDefinition buildFlattenedDataSetDefinition() {
		FlattenDataSetWithDatesDataSetDefinition dsd = new FlattenDataSetWithDatesDataSetDefinition();
		dsd.setJoinOnColumn("patient_id");
		dsd.setSortByColumn("encounter_datetime");
		dsd.setBaseDefinition(Mapped.noMappings(buildRowPerEncounterDataSetDefinition()));
		return dsd;
	}
	
	private ObsForEncounterDataDefinition colHelper(int conceptId) {
		ObsForEncounterDataDefinition col = new ObsForEncounterDataDefinition();
		col.setQuestion(conceptService.getConcept(conceptId));
		return col;
	}
	
}
