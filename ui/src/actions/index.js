// @flow

import type {Dispatch, AnalysesMetadata, Analysis, Dataset} from "../utils.flowtype";
import config from "../app.config";

export const GET_ANALYSES = "GET_ANALYSES";

export const  RECEIVE_ANALYSES = "RECEIVE_ANALYSES";
export function receiveAnalyses(api:string, json:AnalysesMetadata) {
	return (dispatch: Dispatch) => {
		dispatch({
			type: RECEIVE_ANALYSES,
			analyses: json,
			receivedAt: Date.now()
		});
	};
}

export const RECEIVE_ANALYSIS = "RECEIVE_ANALYSIS";
export function receiveAnalysis(api: string, json:Analysis) {
	console.log(json);
	return(dispatch: Dispatch) => {
		dispatch({
			type: RECEIVE_ANALYSIS,
			analysis: json,
			receivedAt: Date.now()
		});
	};
}

export const RECEIVE_DATASETS = "RECEIVE_DATASETS";

export function receiveDatasets(json: Dataset) {
	return(dispatch: Dispatch) => {
		dispatch({
			type: RECEIVE_DATASETS,
			datasets: json,
			receivedAt: Date.now()
		});
	};
}

export function fetchAnalyses() {

	const endpoint = `${config.maestroService  }/maestro/api/analyses/metadata`;

	return (dispatch: Dispatch) => {
		return fetch(endpoint)
			.then(response => response.json())
			.then( json =>
				dispatch(receiveAnalyses(config.maestroService, json))
			);
	};
}

export function getAnalysisById(id: String) {
	//TODO: Move to a configuration file
	const endpoint = `${config.maestroService  }/maestro/api/analyses/${  id}`;

	return (dispatch: Dispatch) => {
		return fetch(endpoint)
			.then(response => response.json())
			.then(json =>
				dispatch(receiveAnalysis(config.maestroService, json))
			);
	};

}

export function fetchDatasets() {
	const endpoint = config.dataService;

	return (dispatch: Dispatch) => {
		return fetch(endpoint)
			.then(response => response.json())
			.then(json =>
				dispatch(receiveDatasets(json))
			);
	};
}

export function login(username, password) {

	const endpoint = `${config.maestroService  }/maestro/api/login`;
	// Currently CORS error due to the header
	// fetch(endpoint, {
	// 	method: "GET",
	// 	headers: {
	// 		"Authorization": `Basic ${window.btoa(`${username }:${ password}`)}`
	// 	}
	// })
	// 	.then(response => response.json())
	// 	.then(json=> console.log(json));
	return;
}

export const RECEIVE_EXECUTION_ID = "RECEIVE_WORKFLOW_ID"
export function receiveDatawolfResponse(json) {
	// Get the id of the layers in geoserver to display in the map
	// Get the info from a table to display
	return(dispatch: Dispatch) => {
		dispatch({
			type: RECEIVE_EXECUTION_ID,
			executionId: json,
			receivedAt: Date.now()
		});
	};
}
export function executeDatawolfWorkflow(workflowid, creatorid, title, description, parameters, datasets) {
	const datawolfUrl = `${config.dataWolf  }executions`;
	const dataToSubmit = {
		"title": title,
		"parameters": parameters,
		"datasets": datasets,
		"workflowId": workflowid,
		"creatorId": creatorid,
		"description": description
	};
	const headers = new Headers({
		"Content-Type": "application/json",
		"Authorization": `Basic ${  btoa("incore-dev@lists.illinois.edu:resilience2017")}`
	});

	return (dispatch: Dispatch) => {
		return fetch(datawolfUrl, {
			method: "POST",
			headers: headers,
			body: JSON.stringify(dataToSubmit),
			credentials: "include",
		}).then(function (response) {
			return response.text();
		}).then(json =>
			dispatch(receiveDatawolfResponse(json))
		);
	};


}
