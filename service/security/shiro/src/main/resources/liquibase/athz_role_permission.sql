-- *******************************************************************************
-- Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
--
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Eurotech - initial API and implementation
-- *******************************************************************************

--liquibase formatted sql

--changeset role_permission:1

CREATE TABLE athz_role_permission (
  scope_id             		BIGINT(21) 	  UNSIGNED NOT NULL,
  id                     	BIGINT(21) 	  UNSIGNED NOT NULL,
  created_on             	TIMESTAMP(3)  NOT NULL,
  created_by             	BIGINT(21)    UNSIGNED NOT NULL,

  role_id             	    BIGINT(21) 	  UNSIGNED,
  
  domain					VARCHAR(64)   NOT NULL,
  action					VARCHAR(64),
  target_scope_id		    BIGINT(21)	  UNSIGNED,
  group_id             	    BIGINT(21) 	  UNSIGNED,
  
  PRIMARY KEY (id),
--  FOREIGN KEY (role_id) REFERENCES athz_role(id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX idx_role_permission_scope_id ON athz_role_permission (role_id, domain, action, target_scope_id, group_id);

INSERT INTO athz_role_permission
	VALUES
		(1,  1, NOW(), 1, 1, 'account', 			null, null, null),

		(1,  2, NOW(), 1, 1, 'broker', 				null, null, null),

		(1,  3, NOW(), 1, 1, 'device',				null, null, null),
		(1,  4, NOW(), 1, 1, 'device_connection', 	null, null, null),
		(1,  5, NOW(), 1, 1, 'device_event', 		null, null, null),
		(1,  6, NOW(), 1, 1, 'device_lifecycle', 	null, null, null),

		(1,  7, NOW(), 1, 1, 'device_management',	null, null, null),

		(1,  8, NOW(), 1, 1, 'datastore', 				null, null, null),

		(1,  9, NOW(), 1, 1, 'credential',			null, null, null),
		(1,	10, NOW(), 1, 1, 'access_token',		null, null, null),

		(1, 11, NOW(), 1, 1, 'access_info',			null, null, null),
		(1, 12, NOW(), 1, 1, 'role',				null, null, null),

		(1, 13, NOW(), 1, 1, 'user',				null, null, null),

		(1, 14, NOW(), 1, 1, 'domain',				null, null, null),

		(1, 15, NOW(), 1, 1, 'group',       null, null, null),

		(2, 16, NOW(), 1, 2, 'account', null, null, null),
		(2, 17, NOW(), 1, 2, 'user', null, null, null),
		(2, 18, NOW(), 1, 2, 'device_event', null, null, null),
		(2, 19, NOW(), 1, 2, 'device_connection', null, null, null),
		(2, 20, NOW(), 1, 2, 'device', null, null, null),
		(2, 21, NOW(), 1, 2, 'datastore', null, null, null),
		(2, 22, NOW(), 1, 2, 'broker', null, null, null),
		(2, 23, NOW(), 1, 2, 'credential', null, null, null),
		(2, 24, NOW(), 1, 2, 'role', null, null, null),
		(2, 25, NOW(), 1, 2, 'domain',				null, null, null),
		(2, 26, NOW(), 1, 2, 'device_lifecycle', null, null, null),
		(2, 27, NOW(), 1, 2, 'device_management', null, null, null),
		(2,	28, NOW(), 1, 2, 'access_token',		null, null, null),
		(2, 29, NOW(), 1, 2, 'access_info',			null, null, null),
    (2, 30, NOW(), 1, 2, 'group',       null, null, null);
