import {SET_USER, LOGIN_ERROR, LOGOUT} from "../actions";
import type {UserState} from "../utils/flowtype";

type UserAction = {
	type: SET_USER,
	username: String,
	auth_token: String,
	loginError: boolean,
	locationFrom: string
}
const defaultState = {username: "", auth_token: "", loginError: false};

const user = (state: UserState = defaultState, action: UserAction) => {
	switch(action.type) {
	case SET_USER:
		return Object.assign({}, state, {username: action.username, auth_token: action.auth_token, loginError: false});
	case LOGIN_ERROR:
		return Object.assign({}, state, {username: "", auth_token: "", loginError: true, locationFrom:sessionStorage.getItem("locationFrom")});
	case LOGOUT:
		return Object.assign({}, state, {username: "", auth_token: "", loginError: false});
	default:
		return state;
	}
};

export default user;
