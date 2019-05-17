import {RECEIVE_SPACES} from "../actions";

type SpaceAction = {
	type: RECEIVE_SPACES,
	spaces: []
}
const defaultState = {spaces: []};

const spaces = (state=defaultState, action: SpaceAction) => {
	switch(action.type) {
	case RECEIVE_SPACES:
		return Object.assign({}, state, {spaces: action.spaces});
	default:
		return state;
	}
};

export default spaces;
