import {RECEIVE_EXECUTION_ID} from "../actions";
import {ExecutionState} from "../utils/flowtype";

type ExecutionAction = {
	type: RECEIVE_EXECUTION_ID,
	executionId: string
}

const defaultState = {executionId: []};

const executions = (state: ExecutionState = defaultState, action: ExecutionAction) => {
	switch(action.type) {
	case RECEIVE_EXECUTION_ID:
		return Object.assign({}, state, {executionId: action.executionId});
	default:
		return state;
	}
};

export default executions;
