// @flow

import type {Dispatch, AnalysesMetadata, Analysis, Dataset, GetState} from "../utils.flowtype";
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
		return fetch(endpoint, {
			headers: getHeader()
		})
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
		return fetch(endpoint, {
			headers: getHeader()
		})
			.then(response => response.json())
			.then(json =>
				dispatch(receiveAnalysis(config.maestroService, json))
			);
	};

}

export function fetchDatasets() {
	const endpoint = config.dataService;

	return (dispatch: Dispatch) => {
		return fetch(endpoint, {
			headers: getHeader()
		})
			.then(response => response.json())
			.then(json =>
				dispatch(receiveDatasets(json))
			);
	};
}

export async function loginHelper(username, password) {
	const endpoint = config.authService;
	// Currently CORS error due to the header
	const userRequest =  await fetch(endpoint, {
		method: "GET",
		headers: {
			"Authorization": `LDAP ${window.btoa(`${username }:${ password}`)}`
		}
	});

	const user = await userRequest.json()   ;

	return user;
}

export const SET_USER = "SET_USER";
export function login(username, password) {

	return async(dispatch: Dispatch) => {

		const json = await loginHelper(username, password);
		return dispatch({
			type: SET_USER,
			username: json["user"],
			auth_token: json["auth-token"]
		});
	};
}

export const RECEIVE_EXECUTION_ID = "RECEIVE_WORKFLOW_ID";
export function receiveDatawolfResponse(json) {
	// Get the id of the layers in geoserver to display in the map
	// Get the info from a table to display


	return (dispatch: Dispatch) => {
		dispatch({
			type: RECEIVE_EXECUTION_ID,
			executionId: json,
			receivedAt: Date.now()
		});


	};

}

async function getOutputDatasetHelper() {
	const executionId = "ffe2e458-b3ba-4d91-a171-9f5176abd872";// state.execution.executionId;
	const datawolfUrl = `${config.dataWolf  }executions/${executionId}`;
	const headers = new Headers({
		"Authorization": `Basic ${  btoa("incore-dev@lists.illinois.edu:resilience2017")}`
	});
	const datawolf_execution_fetch = await fetch(datawolfUrl, {
		method: "GET",
		headers: headers
	});

	const datawolfExecution  = await datawolf_execution_fetch.json();

	const output_dataset_id =datawolfExecution.datasets["fb2ff2f0-5708-4b29-c701-f3a6288021eb"];

	const endpoint = `${config.dataService   }/${   output_dataset_id}` ;
	const output_dataset = await fetch(endpoint, {
		headers: getHeader()
	});

	const outputDataset = await output_dataset.json();
	const fileId = outputDataset.fileDescriptors[0].id;

	const fileDownloadUrl = `${config.dataService }/files/${  fileId  }/file`;
	const fileBlob = await fetch(fileDownloadUrl, {method: "GET", mode: "CORS", headers: getHeader()});

	const fileText = await fileBlob.text();

	return [outputDataset.id, fileText];
}

export const RECEIVE_OUTPUT = "RECEIVE_OUTPUT";
export function getOutputDataset() {
	 // const state = getState();

	return async (dispatch: Dispatch) => {
		const data = await getOutputDatasetHelper();
		 dispatch({
			type: RECEIVE_OUTPUT,
			outputDatasetId: data[0],
			file: data[1].replace(/"/g,"").split("\n")
		});

	};
}

export async function executeDatawolfWorkflowHelper(workflowid, creatorid, title, description, parameters, datasets) {
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

	const datawolfExecution = await fetch(datawolfUrl, {
		method: "POST",
		headers: headers,
		body: JSON.stringify(dataToSubmit),
		credentials: "include",
	});

	const executionId = await datawolfExecution.text();

	return executionId;
}

export function executeDatawolfWorkflow(workflowid, creatorid, title, description, parameters, datasets) {

	return async (dispatch: Dispatch) =>
	{
		 const json = await  executeDatawolfWorkflowHelper(workflowid, creatorid, title, description, parameters, datasets);
		return dispatch({
			type: RECEIVE_EXECUTION_ID,
			executionId: json,
			receivedAt: Date.now()
		});
	};

}

function getHeader() {
	return(dispatch, getState) => {
		const state = getState();
		const headers = new Headers({
			"Authorization": "LDAP token",
			"auth_user": state.user.username,
			"auth_token": state.user.token
		});
		return headers;
	};

}
