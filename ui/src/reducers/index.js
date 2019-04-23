import { combineReducers } from "redux";
import { routerReducer } from "react-router-redux";
import analyses from "./analyses";
import datasets from "./datasets";
import hazards from "./hazards";
import executions from "./executions";
import user from "./user";
import spaces from "./spaces";

const rootReducer = combineReducers({
	routing: routerReducer,
	analyses: analyses,
	data: datasets,
	execution: executions,
	user: user,
	hazard: hazards,
	space: spaces
});

export default rootReducer;
