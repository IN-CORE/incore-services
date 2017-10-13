import {connect} from "react-redux";
import {fetchAnalyses} from "../actions/index";
import ExecuteAnalysisComponent from "../components/ExecuteAnalysis";
import type { Dispatch } from "../utils/flowtype";

const mapDispatchToProps = (dispatch: Dispatch) => {
	return {
		loadAnalyses: () => {
			dispatch(fetchAnalyses());
		}
	};
};

const ExecuteAnalysis = connect(null, mapDispatchToProps)(ExecuteAnalysisComponent);

export default ExecuteAnalysis;
