import {connect} from "react-redux";
import {fetchAnalyses, executeDatawolfWorkflow, fetchDatasets} from "../actions/index";
import ExecuteAnalysisComponent from "../components/ExecuteAnalysis";
import type { Dispatch } from "../utils/flowtype";

const mapStateToProps = (state, ownProps) => {
	return {
		analysis: state.analyses.selectedAnalysis,
		datasets: state.data.datasets
	};
};


const mapDispatchToProps = (dispatch: Dispatch) => {
	return {
		loadAnalyses: () => {
			dispatch(fetchAnalyses());
		},
		executeAnalysis: (workflowid, creatorid, title, description, parameters, datasets) => {
			dispatch(executeDatawolfWorkflow(workflowid, creatorid, title, description, parameters, datasets));
		},
		loadDatasets: () => {
			dispatch(fetchDatasets());
		}
	};
};

const ExecuteAnalysis = connect(mapStateToProps, mapDispatchToProps)(ExecuteAnalysisComponent);

export default ExecuteAnalysis;
