import {RECEIVE_EXECUTION_ID, RECEIVE_OUTPUT} from "../actions";
import {ExecutionState} from "../utils/flowtype";

type ExecutionAction = {
	type: RECEIVE_EXECUTION_ID | RECEIVE_OUTPUT,
	executionId: ?string,
	outputFile: ?string,
	outputDatasetId: ?string
}

const defaultState = {executionId: [], outputFile: "", outputDatasetId: ""};

const executions = (state: ExecutionState = defaultState, action: ExecutionAction) => {
	switch(action.type) {
	case RECEIVE_EXECUTION_ID:
		return Object.assign({}, state, {executionId: action.executionId});

	case RECEIVE_OUTPUT:
		return Object.assign({}, state, {outputFile: action.file, outputDatasetId: action.outputDatasetId});
	default:
		return state;
	}
};

export default executions;
