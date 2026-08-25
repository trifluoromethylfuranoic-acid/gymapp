INSERT INTO training_type (id, name)
VALUES ('a0000000-0000-0000-0000-000000000001', 'Strength Training'),
       ('a0000000-0000-0000-0000-000000000002', 'Cardiovascular Training'),
       ('a0000000-0000-0000-0000-000000000003', 'Flexibility & Mobility'),
       ('a0000000-0000-0000-0000-000000000004', 'HIIT'),
       ('a0000000-0000-0000-0000-000000000005', 'Yoga'),
       ('a0000000-0000-0000-0000-000000000006', 'Functional Training');

INSERT INTO users (id, first_name, last_name, username, password, is_active, role, failed_login_attempts)
VALUES ('b0000000-0000-0000-0000-000000000001', 'Admin', 'User', 'admin', '{noop}admin123', TRUE, 'ROLE_ADMIN', 0),
       ('b0000000-0000-0000-0000-000000000002', 'John', 'Smith', 'john.smith', '{noop}password123', TRUE,
        'ROLE_TRAINEE', 0),
       ('b0000000-0000-0000-0000-000000000003', 'Emily', 'Davis', 'emily.davis', '{noop}password123', TRUE,
        'ROLE_TRAINEE', 0),
       ('b0000000-0000-0000-0000-000000000004', 'Michael', 'Johnson', 'michael.j', '{noop}password123', TRUE,
        'ROLE_TRAINER', 0),
       ('b0000000-0000-0000-0000-000000000005', 'Sarah', 'Williams', 'sarah.w', '{noop}password123', TRUE,
        'ROLE_TRAINER', 0);

INSERT INTO admin (id, user_id)
VALUES ('e0000000-0000-0000-0000-000000000001',
        'b0000000-0000-0000-0000-000000000001');

INSERT INTO trainee (id, user_id, date_of_birth, address)
VALUES ('c0000000-0000-0000-0000-000000000001',
        'b0000000-0000-0000-0000-000000000002',
        DATE '1995-06-15',
        '123 Main St, Springfield'),
       ('c0000000-0000-0000-0000-000000000002',
        'b0000000-0000-0000-0000-000000000003',
        DATE '1998-03-22',
        '456 Oak Ave, Shelbyville');

INSERT INTO trainer (id, user_id, specialization_id)
VALUES ('d0000000-0000-0000-0000-000000000001',
        'b0000000-0000-0000-0000-000000000004',
        'a0000000-0000-0000-0000-000000000001'),
       ('d0000000-0000-0000-0000-000000000002',
        'b0000000-0000-0000-0000-000000000005',
        'a0000000-0000-0000-0000-000000000005');

INSERT INTO training_assignment (trainee_id, trainer_id)
VALUES ('c0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001'),
       ('c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000002');
