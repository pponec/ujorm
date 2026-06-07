# Ujorm Tutorial Pattern Index

This index starts with basic tutorial scenarios and then continues with advanced patterns.

GitHub source:
- [QuickStartTutorialTest.java](QuickStartTutorialTest.java)
- [TutorialTest.java](TutorialTest.java)
- [AdvancedTutorialTest.java](AdvancedTutorialTest.java)

## Basic Scenarios

| Test Class | Method | When To Use |
|---|---|---|
| `QuickStartTutorialTest.java` | [`insert`](QuickStartTutorialTest.java#L49) | Minimal quick-start insert |
| `QuickStartTutorialTest.java` | [`select_by_column`](QuickStartTutorialTest.java#L77) | Quick-start SQL column mapping |
| `QuickStartTutorialTest.java` | [`select_by_label`](QuickStartTutorialTest.java#L105) | Quick-start SQL label mapping |
| `QuickStartTutorialTest.java` | [`init`](QuickStartTutorialTest.java#L137) | Quick-start schema bootstrap |
| `TutorialTest.java` | [`insert`](TutorialTest.java#L33) | Basic entity insert including relation setup |
| `TutorialTest.java` | [`selectEntity_by_id`](TutorialTest.java#L52) | Simple primary key lookup |
| `TutorialTest.java` | [`select_by_criteron`](TutorialTest.java#L79) | Type-safe criteria composition with joins |
| `TutorialTest.java` | [`select_by_alias`](TutorialTest.java#L103) | Self-reference aliasing patterns |
| `TutorialTest.java` | [`select_group_by`](TutorialTest.java#L134) | Grouping and aggregation projection |
| `TutorialTest.java` | [`select_by_columns`](TutorialTest.java#L153) | Native SQL with explicit column mapping |
| `TutorialTest.java` | [`select_by_labels`](TutorialTest.java#L182) | Native SQL with label mapping strategy |
| `TutorialTest.java` | [`update`](TutorialTest.java#L215) | Batch update of selected attributes |
| `TutorialTest.java` | [`update_state_enum`](TutorialTest.java#L235) | Enum persistence and verification |
| `TutorialTest.java` | [`delete_by_criterion`](TutorialTest.java#L281) | Direct SQL DELETE with type-safe Criterion filter |
| `TutorialTest.java` | [`delete`](TutorialTest.java#L311) | FK-safe delete ordering |
| `TutorialTest.java` | [`init`](TutorialTest.java#L292) | Schema bootstrap for tutorial scenario |

## Advanced Scenarios

| Pattern | Test Method | When To Use |
|---|---|---|
| Insert defaults | [`micro_insert_with_explicit_default_fields`](AdvancedTutorialTest.java#L46) | Verify generated defaults and initial entity state |
| Not-found read contract | [`micro_find_by_id_not_found_contract`](AdvancedTutorialTest.java#L58) | Distinguish empty Optional vs nullable lookup |
| Minimal projection | [`micro_select_only_required_columns`](AdvancedTutorialTest.java#L68) | Avoid over-fetching in read-heavy paths |
| Self-join alias | [`micro_self_join_alias_pattern`](AdvancedTutorialTest.java#L89) | Query recursive relations (boss/parent) safely |
| Label vs column mapping | [`micro_sql_label_and_column_mapping_equivalence`](AdvancedTutorialTest.java#L107) | Keep SQL readable while preserving mapper correctness |
| Reusable criteria | [`micro_reusable_criteria_composition`](AdvancedTutorialTest.java#L152) | Compose query filters across joins |
| Enum roundtrip | [`micro_enum_state_roundtrip`](AdvancedTutorialTest.java#L173) | Persist and query enum-backed columns |
| Partial update | [`micro_partial_update_single_attribute`](AdvancedTutorialTest.java#L190) | Update one attribute without touching others |
| Stable pagination | [`micro_order_by_for_stable_pagination`](AdvancedTutorialTest.java#L204) | Deterministic pages in UI/API listing |
| Grouped projection | [`micro_group_by_with_typed_projection`](AdvancedTutorialTest.java#L225) | Map aggregates into typed records |
| Batch state update | [`micro_batch_update_with_minimal_projection`](AdvancedTutorialTest.java#L253) | Bulk update selected entity subset |
| FK-safe batch delete | [`micro_batch_delete_fk_safe_order`](AdvancedTutorialTest.java#L275) | Delete hierarchy in safe dependency order |
| FK violation contract | [`failure_constraint_violation_is_predictable`](AdvancedTutorialTest.java#L305) | Assert expected DB constraint failures |
| Null/empty edge case | [`failure_null_and_empty_edge_cases`](AdvancedTutorialTest.java#L316) | Validate domain and DB nullability assumptions |
| Bad bind contract | [`failure_incorrect_bind_contract`](AdvancedTutorialTest.java#L328) | Ensure unresolved parameters fail loudly |
| Not-found mutation | [`failure_not_found_update_affects_no_rows`](AdvancedTutorialTest.java#L345) | Treat zero-updated rows as missing data |
| Rollback on error | [`failure_rollback_on_intermediate_error`](AdvancedTutorialTest.java#L360) | Keep transaction atomic on partial failure |
| Explicit commit | [`transaction_explicit_commit_multi_step`](AdvancedTutorialTest.java#L383) | Finalize a multi-step business operation |
| Explicit rollback | [`transaction_explicit_rollback_multi_step`](AdvancedTutorialTest.java#L405) | Abort and restore pre-transaction state |
| Mixed atomic unit | [`transaction_atomic_mixed_operations`](AdvancedTutorialTest.java#L417) | Validate insert/update/delete all-or-nothing |
| Optimistic conflict | [`locking_optimistic_conflict_pattern`](AdvancedTutorialTest.java#L441) | Detect stale write attempts safely |
| Pessimistic contention | [`locking_pessimistic_contention_pattern`](AdvancedTutorialTest.java#L474) | Demonstrate lock waiting/conflict behavior |
| READ_COMMITTED behavior | [`transaction_isolation_read_committed_allows_non_repeatable_read`](AdvancedTutorialTest.java#L502) | Explain non-repeatable read risk |
| REPEATABLE_READ behavior | [`transaction_isolation_repeatable_read_keeps_snapshot`](AdvancedTutorialTest.java#L536) | Show stable reads inside transaction |
| N+1 prevention | [`performance_prevent_n_plus_one`](AdvancedTutorialTest.java#L575) | Fetch relation data in one query |
| Unordered pagination anti-pattern | [`anti_pattern_unordered_pagination_warning`](AdvancedTutorialTest.java#L606) | Document why missing ORDER BY is risky |
| Index-aware pagination | [`performance_pagination_with_index_aware_filter`](AdvancedTutorialTest.java#L631) | Scale paginated listing with predictable order |
| Column rename migration | [`migration_column_rename_transition`](AdvancedTutorialTest.java#L652) | Transitional read path during rename rollout |
| Default + backfill migration | [`migration_default_and_backfill_strategy`](AdvancedTutorialTest.java#L676) | Evolve schema with safe backfill |
| Null relation mapping | [`boundary_mapper_null_relation_mapping`](AdvancedTutorialTest.java#L710) | Verify LEFT JOIN null relation handling |
| Missing label/column mapping | [`boundary_mapper_missing_label_maps_partial_record`](AdvancedTutorialTest.java#L739) | Show partial mapping risk when labels are incomplete |
| Unknown enum value | [`boundary_enum_unknown_value_fails`](AdvancedTutorialTest.java#L761) | Detect incompatible persisted enum values |
