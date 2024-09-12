import { basicSetup, EditorView } from 'codemirror';
import { EditorState } from '@codemirror/state';
import { graphql } from 'cm6-graphql';

const input = document.getElementById('input');

const output = document.getElementById('output');
const debug = document.getElementById('debug');

const sendBtn = document.getElementById('sendBtn');
const abortBtn = document.getElementById('abortBtn');

const endpoint = document.location.href
const saveKey = "savedRequest";

const queryParamName = "qtxt";

// Do not directly put a query from url into the local store
// Only do so on edits (i.e. don't override local store when clicking on a link)
const queryParams = new URLSearchParams(window.location.search);

function getInitialEditorContentFromQueryString() { // Returns true iff there is a 'query' arg
  const r = queryParams.get(queryParamName);
  return r;
}

function getInitialEditorContent() {
  var r = getInitialEditorContentFromQueryString();
  if (!r) {
    r = localStorage.getItem(saveKey);
  }
  if (!r) {
    r = "";
  }
  return r;
}

/*
function saveEditorState() {
  const v = editorView.state.doc.toString();
  localStorage.setItem(saveKey, v);
}
*/

const startState = EditorState.create({
  doc: getInitialEditorContent(),
  extensions: [
    basicSetup,
    graphql(),
    EditorView.updateListener.of(update => {
      if (update.docChanged) {
        const editorContent = update.state.doc.toString();
        localStorage.setItem(saveKey, editorContent);
      }
    }),
  ],
});

const editorView = new EditorView({
  state: startState,
  parent: input
});

function getEditorContent() {
  const result = editorView.state.doc.toString();
  return result;
}

window.addEventListener('popstate', function (e) {
  input.value = e.state.queryStr;
});

var previousQueryStr = getEditorContent(); // Don't push history states if there was no change'
history.replaceState({ "queryStr": previousQueryStr }, null, null);

var typingTimer;                //timer identifier
var doneTypingInterval = 1000;  //time in ms (5 seconds)

function scheduleSaveState() {
  clearTimeout(typingTimer);
  typingTimer = setTimeout(saveState, doneTypingInterval);
}


function toBashString(str) {
  return str.replaceAll("'", "'\\''")
}

function showCurl() {
  const payloadStr = buildRequest();
  output.value = `curl -X POST '${toBashString(endpoint)}' -d '${toBashString(payloadStr)}'`;
  debug.value = "";
}

/** Create a link if it hasn't happened yet. Pushes a history state on change. */
function createLink() {
  const oldQueryStr = queryParams.get(queryParamName);
  const newQueryStr = getEditorContent();
  if (newQueryStr != oldQueryStr) {
    queryParams.set(queryParamName, newQueryStr);
    const state = { "queryStr": newQueryStr };
    history.pushState(state, null, "?" + queryParams.toString());
  }
}

function buildRequest() {
  const v = getEditorContent();
  const graphQlPayload = {
    query: v
  }
  const r = JSON.stringify(graphQlPayload)
  return r;
}

var pendingXhr = null;

function abortRequest() {
  if (pendingXhr != null) {
    pendingXhr.abort();
  }
}

function setAbortDisabled(value) {
  abortBtn.disabled = value;
  sendBtn.disabled = !value;
}

function sendRequest() {
  const queryStr = getEditorContent();

  if (queryStr != previousQueryStr) {
    const state = { "queryStr": queryStr };
    history.pushState(state, null, null);
    previousQueryStr = queryStr;
  }

  // Abort any pending request
  abortRequest();
  setAbortDisabled(false);

  const payloadStr = buildRequest();

  const xhr = new XMLHttpRequest();
  xhr.onreadystatechange = () => {
    // Only handle requests for the latest pending xhr
    if (xhr == pendingXhr) {
      // 0 = UNSENT (aborted), 4 = DONE
      if (xhr.readyState == 0 || xhr.readyState == 4) {
        setAbortDisabled(true);

        if (xhr.status == 200) {
          const data = xhr.response;
          output.value = data;

          const json = JSON.parse(data);
          // Check for extensions.metadata keys
          var debugStr = "";
          const meta = json?.extensions?.metadata;
          if (meta) {
            for (const [key, value] of Object.entries(meta)) {
              const sparqlQuery = value?.sparqlQuery;
              if (sparqlQuery) {
                debugStr += `Entry ${key}:\n${sparqlQuery}\n\n`;
              }
            }
          }
          debug.value = debugStr
        } else {
          if (xhr.status == 0) {
            // Retain the current output
            // output.value = "Request aborted";
          } else {
            output.value = `Error: ${xhr.status}\n\n${xhr.responseText}`;
            debug.value = "";
          }
        }
        pendingXhr = null;
      } else {
        setAbortDisabled(false);
      }
    }
  }
  xhr.open("POST", endpoint);
  xhr.send(payloadStr);
  pendingXhr = xhr;
}

window.sendRequest = sendRequest;
window.abortRequest = abortRequest;
window.showCurl = showCurl;
window.createLink = createLink;
window.scheduleSaveState = scheduleSaveState;




