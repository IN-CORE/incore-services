import {SET_USER} from "../actions";
import type {UserState} from "../utils/flowtype";

type UserAction = {
	type: SET_USER,
	username: String,
	auth_token: String
}
const defaultState = {username: "", auth_token: ""};

const user = (state: UserState = defaultState, action: UserAction) => {
	switch(action.type) {
	case SET_USER:
		return Object.assign({}, state, {username: action.username, auth_token: action.auth_token});
	default:
		return state;
	}
};

export default user;
