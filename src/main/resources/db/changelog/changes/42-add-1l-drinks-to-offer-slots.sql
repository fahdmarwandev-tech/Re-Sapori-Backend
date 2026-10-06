-- liquibase formatted sql

-- changeset resapori:42-add-1l-drinks-to-offer-slots runOnChange:true
-- comment: Add 1 Liter drinks to Friends Meal and Family Meal offer slots

INSERT INTO offer_slot_eligible_items (slot_id, menu_item_id)
VALUES
    -- Friends Meal drink slot
    ('a0000000-0000-0000-0000-000000000007', 'ffa48f05-fae4-46d2-9865-7c0c81605ec2'),
    ('a0000000-0000-0000-0000-000000000007', '375d90f3-99be-4471-92f9-cab540ef3c48'),
    ('a0000000-0000-0000-0000-000000000007', 'ae7e23d3-7dad-425a-a8cf-bb6c78504e8f'),
    -- Family Meal drink slot
    ('a0000000-0000-0000-0000-00000000000b', 'ffa48f05-fae4-46d2-9865-7c0c81605ec2'),
    ('a0000000-0000-0000-0000-00000000000b', '375d90f3-99be-4471-92f9-cab540ef3c48'),
    ('a0000000-0000-0000-0000-00000000000b', 'ae7e23d3-7dad-425a-a8cf-bb6c78504e8f')
ON CONFLICT DO NOTHING;
