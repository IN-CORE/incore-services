import { combineReducers } from "redux";
import { routerReducer } from "react-router-redux";
import analyses from "./analyses";
import datasets from "./datasets";
import executions from "./executions";

const rootReducer = combineReducers({
	routing: routerReducer,
	analyses: analyses,
	data: datasets,
	execution: executions
});

export default rootReducer;
