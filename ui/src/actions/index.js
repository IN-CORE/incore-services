// @flow

import type {Dispatch, AnalysesMetadata} from "../utils.flowtype";

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

export function fetchAnalyses() {
	//TODO: Move to a configuration file
	const api = "http://localhost:8080";
	const endpoint = api + "/maestro/api/analysis";

	return (dispatch: Dispatcch) => {
		return fetch(endpoint)
			.then(response => response.json())
			.then( json =>
				dispatch(receiveAnalyses(api, json))
			);
	}
}
