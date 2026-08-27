-- Rivermap (https://api.rivermap.org) aggregates gauges of many authorities; we fetch their stations and readings
-- through the Rivermap API, so for us it is a provider. Own migration file: a value added with ADD VALUE cannot be
-- used in the same transaction it was added in.
ALTER TYPE provider ADD VALUE 'RIVERMAP';
