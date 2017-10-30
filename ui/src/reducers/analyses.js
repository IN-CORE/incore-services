
import {RECEIVE_ANALYSES, RECEIVE_ANALYSIS} from "../actions";
import type {AnalysesMetadata, AnalysesState, Analysis} from "../utils/flowtype";

type AnalysesAction = {|
	type: RECEIVE_ANALYSES | RECEIVE_ANALYSIS,
	analyses: AnalysesMetadata,
	analysis: Analysis,
	receivedAt: Date
|};
const defaultState = {analysesMetadata: [], selectedAnalysis: null};

const analyses = (state: AnalysesState = defaultState, action:AnalysesAction) => {

	switch(action.type) {
	case RECEIVE_ANALYSES:
		return Object.assign({}, state, {
			analysesMetadata: action.analyses,
		});

	case RECEIVE_ANALYSIS:
		return Object.assign({}, state, {
			selectedAnalysis: action.analysis,
		});

	default:
		return state;
	}

};

export default analyses;
