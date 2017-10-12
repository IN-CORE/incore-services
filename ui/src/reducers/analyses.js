
import {RECEIVE_ANALYSES} from "../actions";
import type {AnalysesMetadata, AnalysesState} from "../utils/flowtype";

type AnalysesAction = {|
	type: RECEIVE_ANALYSES,
	analyses: AnalysesMetadata,
	receivedAt: Date
|};
const defaultState = {analysesMetadata: []};

const analyses = (state: AnalysesState = defaultState, action:AnalysesAction) => {

	switch(action.type) {
	case RECEIVE_ANALYSES:
		return Object.assign({}, state, {
			analysesMetadata: action.analyses,
		});

	default:
		return state;
	}

};

export default analyses;
