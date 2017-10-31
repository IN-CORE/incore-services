import {RECEIVE_DATASETS} from "../actions";
import {DatasetState, Dataset} from "../utils/flowtype";

type DatasetAction = {
	type: RECEIVE_DATASETS,
	datasets: Dataset[]
}
const defaultState = {datasets: []};

const datasets = (state: DatasetState = defaultState, action: DatasetAction) => {
	switch(action.type) {
	case RECEIVE_DATASETS:
		return Object.assign({}, state, {datasets: action.datasets});
	default:
		return state;
	}
};

export default datasets;
