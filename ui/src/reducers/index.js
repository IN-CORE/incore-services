import { combineReducers } from "redux";
import { routerReducer } from "react-router-redux";
import analyses from "./analyses";

const rootReducer = combineReducers({
	routing: routerReducer,
	analyses: analyses
});

export default rootReducer;
