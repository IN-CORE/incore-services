import { combineReducers } from "redux";
import { routerReducer } from "react-router-redux";
import analyses from "./analyses";
import datasets from "./datasets";

const rootReducer = combineReducers({
	routing: routerReducer,
	analyses: analyses,
	data: datasets
});

export default rootReducer;
