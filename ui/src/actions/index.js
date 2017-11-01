// @flow

import type {Dispatch, AnalysesMetadata, Analysis, Dataset} from "../utils.flowtype";

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
	//TODO: Move to a configuration file
	const api = "http://localhost:8080";
	const endpoint = `${api  }/maestro/api/analyses/metadata`;

	return (dispatch: Dispatch) => {
		return fetch(endpoint)
			.then(response => response.json())
			.then( json =>
				dispatch(receiveAnalyses(api, json))
			);
	};
}

export function getAnalysisById(id: String) {
	//TODO: Move to a configuration file
	const api = "http://localhost:8080";
	const endpoint = `${api  }/maestro/api/analyses/${  id}`;

	return (dispatch: Dispatch) => {
		return fetch(endpoint)
			.then(response => response.json())
			.then(json =>
				dispatch(receiveAnalysis(api, json))
			);
	};

}

export function fetchDatasets() {
	//TODO: Move to a configuration file
	const api = "http://141.142.210.193:8888";
	const endpoint = `${api  }/data/api/datasets/list/`;

	return (dispatch: Dispatch) => {
		return fetch(endpoint)
			.then(response => response.json())
			.then(json =>
				dispatch(receiveDatasets(json))
			);
	};
}

export function login(username, password) {

	const api = "http://localhost:8080";
	const endpoint = `${api  }/maestro/api/login`;
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

export function receiveDatawolfResponse(json) {
	// Get the id of the layers in geoserver to display in the map
	// Get the info from a table to display

}
export function executeDatawolfWorkflow(workflowid, creatorid, title, description, parameters, datasets) {
	const datawolfUrl = "http://141.142.209.63/datawolf/" + "executions";
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
		"Accept": "application/json",
	});

	console.log(JSON.stringify(dataToSubmit, undefined, 2));
	return (dispatch: Dispatch) => {
		return fetch(datawolfUrl, {
			method: "POST",
			data: JSON.stringify(dataToSubmit),
			dataType: "text",
			headers: headers
		}).then(function (response) {
			console.log(response);
			return response.json;
		}).then(json =>
			dispatch(receiveDatawolfResponse(json))
		);
	};


}
