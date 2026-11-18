check "PUT update project" "200"

# Application APIs
APPLICATION_ID="$(apply_to_project "$PROJECT_ID" "$FREELANCER_TOKEN")"
stop_if_missing "Application ID" "$APPLICATION_ID"
request GET "$API/api/projects/$PROJECT_ID/applications" "" "$CLIENT_TOKEN"
check "Client lists project applications" "200"
request GET "$API/api/applications/my-applications" "" "$FREELANCER_TOKEN"
check "Freelancer lists own applications" "200"
request PUT "$API/api/projects/$PROJECT_ID/applications/$APPLICATION_ID/accept" "" "$CLIENT_TOKEN"
check "Client accepts freelancer application" "200"
request GET "$API/api/projects/my-projects" "" "$FREELANCER_TOKEN"
check "GET /api/projects/my-projects (freelancer)" "200"

# Escrow happy path: deposit -> submit -> revision -> resubmit -> approve -> release
ESCROW_ID="$(create_escrow "$PROJECT_ID" "$CLIENT_TOKEN")"
stop_if_missing "Main escrow ID" "$ESCROW_ID"
request GET "$API/api/escrows/my-escrows" "" "$CLIENT_TOKEN"
check "GET client my-escrows" "200"
request GET "$API/api/escrows/$ESCROW_ID" "" "$CLIENT_TOKEN"
check "GET escrow by ID" "200"

record_tx "$ESCROW_ID" "$CLIENT_TOKEN" DEPOSIT \
  "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

SUBMIT_BODY='{
  "description":"Initial implementation submitted for review.",
  "fileUrl":"https://github.com/octocat/Hello-World",
  "repoUrl":"https://github.com/octocat/Hello-World",
  "taskDescription":"Review this public GitHub repository and assess whether it contains a working sample project with source files and documentation.",
  "acceptanceCriteria":[
    "The repository is publicly accessible.",
    "The repository contains project source files.",
    "The repository contains a README or project documentation."
  ]
}'
request POST "$API/api/escrows/$ESCROW_ID/submit" "$SUBMIT_BODY" "$FREELANCER_TOKEN"
check "Freelancer submits deliverable (AI verification is attempted)" "201"
DELIVERABLE_ID="$(json_field id <<<"$RESPONSE_BODY")"
need_value "Submission response contains deliverable ID" "$DELIVERABLE_ID"
AI_STATUS="$(json_field verificationStatus <<<"$RESPONSE_BODY")"
AI_VERDICT="$(json_field aiVerdict <<<"$RESPONSE_BODY")"
if [[ "$AI_STATUS" == "COMPLETED" || "$AI_STATUS" == "FAILED" ]]; then
  printf 'PASS  %-58s %s (verdict=%s)\n' "AI verification result persisted" "$AI_STATUS" "${AI_VERDICT:-not returned}"
  PASS_COUNT=$((PASS_COUNT+1))
else
  printf 'FAIL  %-58s unexpected/missing value: %s\n' "AI verification result persisted" "${AI_STATUS:-<empty>}"
  FAIL_COUNT=$((FAIL_COUNT+1))
fi
stop_if_missing "Deliverable ID" "$DELIVERABLE_ID"

request GET "$API/api/escrows/$ESCROW_ID/deliverables" "" "$CLIENT_TOKEN"
check "GET escrow deliverables list" "200"
request POST "$API/api/escrows/$ESCROW_ID/deliverables/$DELIVERABLE_ID/revision" \
  '{"feedback":"Please improve the documentation and clarify the implementation details."}' \
  "$CLIENT_TOKEN"
check "Client requests deliverable revision" "200"

# Resubmit after revision (version 2)
request POST "$API/api/escrows/$ESCROW_ID/submit" "$SUBMIT_BODY" "$FREELANCER_TOKEN"
check "Freelancer resubmits revised deliverable" "201"
DELIVERABLE_ID_2="$(json_field id <<<"$RESPONSE_BODY")"
stop_if_missing "Second deliverable ID" "$DELIVERABLE_ID_2"
request POST "$API/api/escrows/$ESCROW_ID/deliverables/$DELIVERABLE_ID_2/approve" "" "$CLIENT_TOKEN"
check "Client approves deliverable" "200"
request GET "$API/api/escrows/$ESCROW_ID/transactions" "" "$CLIENT_TOKEN"
check "GET escrow transaction history" "200"
record_tx "$ESCROW_ID" "$CLIENT_TOKEN" RELEASE \
  "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
request GET "$API/api/escrows/$ESCROW_ID" "" "$FREELANCER_TOKEN"
check "Freelancer can retrieve own escrow after release" "200"

# Dispute/refund path on a second independent project
PROJECT_2_ID="$(create_project "API Workflow Dispute $RUN_ID" "$CLIENT_TOKEN")"
stop_if_missing "Dispute project ID" "$PROJECT_2_ID"
APPLICATION_2_ID="$(apply_to_project "$PROJECT_2_ID" "$FREELANCER_TOKEN")"
stop_if_missing "Dispute application ID" "$APPLICATION_2_ID"
request PUT "$API/api/projects/$PROJECT_2_ID/applications/$APPLICATION_2_ID/accept" "" "$CLIENT_TOKEN"
check "Accept freelancer for dispute project" "200"
ESCROW_2_ID="$(create_escrow "$PROJECT_2_ID" "$CLIENT_TOKEN")"
stop_if_missing "Dispute escrow ID" "$ESCROW_2_ID"
record_tx "$ESCROW_2_ID" "$CLIENT_TOKEN" DEPOSIT \
  "0xcccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
request POST "$API/api/escrows/$ESCROW_2_ID/submit" "$SUBMIT_BODY" "$FREELANCER_TOKEN"
check "Submit deliverable for dispute workflow" "201"
request POST "$API/api/escrows/$ESCROW_2_ID/dispute" \
  '{"reason":"Automated smoke test: disputed deliverable for testing dispute workflow."}' \
  "$CLIENT_TOKEN"
check "Raise escrow dispute" "200"
request GET "$API/api/escrows/$ESCROW_2_ID" "" "$CLIENT_TOKEN"
check "Retrieve disputed escrow" "200"
record_tx "$ESCROW_2_ID" "$CLIENT_TOKEN" REFUND \
  "0xdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
request GET "$API/api/escrows/$ESCROW_2_ID/transactions" "" "$CLIENT_TOKEN"
check "Get transactions after refund" "200"

# Additional application lifecycle APIs: reject and withdraw on separate projects
PROJECT_3_ID="$(create_project "API Workflow Withdraw $RUN_ID" "$CLIENT_TOKEN")"
stop_if_missing "Withdraw project ID" "$PROJECT_3_ID"
APPLICATION_3_ID="$(apply_to_project "$PROJECT_3_ID" "$FREELANCER_TOKEN")"
stop_if_missing "Withdraw application ID" "$APPLICATION_3_ID"
request DELETE "$API/api/projects/$PROJECT_3_ID/applications/my-application" "" "$FREELANCER_TOKEN"
check "Freelancer withdraws own application" "204"

PROJECT_4_ID="$(create_project "API Workflow Reject $RUN_ID" "$CLIENT_TOKEN")"
stop_if_missing "Reject project ID" "$PROJECT_4_ID"
APPLICATION_4_ID="$(apply_to_project "$PROJECT_4_ID" "$FREELANCER_TOKEN")"
stop_if_missing "Reject application ID" "$APPLICATION_4_ID"
request PUT "$API/api/projects/$PROJECT_4_ID/applications/$APPLICATION_4_ID/reject" "" "$CLIENT_TOKEN"
check "Client rejects application" "204"

# Delete an unused open project
PROJECT_5_ID="$(create_project "API Workflow Delete $RUN_ID" "$CLIENT_TOKEN")"
stop_if_missing "Delete project ID" "$PROJECT_5_ID"
request DELETE "$API/api/projects/$PROJECT_5_ID" "" "$CLIENT_TOKEN"
check "Client deletes unused open project" "204"

# Role guards
request POST "$API/api/projects" \
  '{"title":"Should Not Be Created","description":"Freelancer role must not create projects.","budget":100}' \
  "$FREELANCER_TOKEN"
check "Freelancer cannot create a project" "403"

summary
[[ "$FAIL_COUNT" -eq 0 ]] && exit 0 || exit 1
