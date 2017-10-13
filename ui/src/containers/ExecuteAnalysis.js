import {connect} from "react-redux";
import {fetchAnalyses} from "../actions/index";
import ExecuteAnalysisComponent from "../components/ExecuteAnalysis";
import type { Dispatch } from "../utils/flowtype";

const mapStateToProps = (state, ownProps) => {
	return {
		analysis: state.analyses.selectedAnalysis
	};
};


const mapDispatchToProps = (dispatch: Dispatch) => {
	return {
		loadAnalyses: () => {
			dispatch(fetchAnalyses());
		}
	};
};

const ExecuteAnalysis = connect(mapStateToProps, mapDispatchToProps)(ExecuteAnalysisComponent);

export default ExecuteAnalysis;
