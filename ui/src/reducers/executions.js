import {RECEIVE_EXECUTION_ID, RECEIVE_OUTPUT_FILE} from "../actions";
import {ExecutionState} from "../utils/flowtype";

type ExecutionAction = {
	type: RECEIVE_EXECUTION_ID,
	executionId: string
}

const defaultState = {executionId: [], outputFile: ""};

const executions = (state: ExecutionState = defaultState, action: ExecutionAction) => {
	switch(action.type) {
	case RECEIVE_EXECUTION_ID:
		return Object.assign({}, state, {executionId: action.executionId});

	case RECEIVE_OUTPUT_FILE:
		return Object.assign({}, state, {outputFile: action.file});
	default:
		return state;
	}
};

export default executions;
