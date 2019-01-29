import {RECEIVE_HAZARDS} from "../actions";
import {HazardState, Hazards} from "../utils/flowtype";

type HazardAction = {
	type: RECEIVE_HAZARDS,
	hazards: Hazards
}
const defaultState = {hazards: []};

const hazards = (state: HazardState = defaultState, action: HazardAction) => {
	switch(action.type) {
	case RECEIVE_HAZARDS:
		return Object.assign({}, state, {hazards: action.hazards});
	default:
		return state;
	}
};

export default hazards;
